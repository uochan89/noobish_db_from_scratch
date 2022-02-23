package index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import page.LeafPage;
import page.NoneLeafPage;

class BTreeTest {

  private static final String TREE_NAME = "BTreeTest";
  private BTree tree = null;


  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {
    tree = BTree.getBTree(TREE_NAME);
  }

  @AfterEach
  void tearDown() throws Exception {
    FileStorage.deleteBTree(TREE_NAME);
  }

  @Test
  void testSingleInsertAndRead() {
    tree.insert(11, 7);
    assertEquals(7, tree.read(11));
  }

  @Test
  void testMultipleInsertAndRead() {
    Random rand = new Random();
    // generate keys
    List<Integer> keys = new ArrayList<Integer>();
    for (int i = 0; i < 100; i++) {
      keys.add(i);
    }
    Collections.shuffle(keys);

    for (int i = 0; i < 100; i++) {
      int key = keys.get(i);
      int value = rand.nextInt((int) Math.pow(2, 31));
      System.out.println("testing kv : " + key + ", " + value);
      tree.insert(key, value);
      assertEquals(value, tree.read(key));
    }

  }

  @Test
  void testInsertingBiggerValue() {
    tree.insert(5, 20);
    tree.insert(6, 23342);
    tree.insert(87, 123);
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals(20, tree.read(5));
    assertEquals(23342, tree.read(6));
    assertEquals(123, tree.read(87));
  }

  @Test
  void testInsertingSmallerValue() {
    tree.insert(87, 20);
    tree.insert(20, 23342);
    tree.insert(5, 20);
    System.out.println("wait 5 sec");
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertEquals(20, tree.read(87));
    assertEquals(23342, tree.read(20));
    assertEquals(20, tree.read(5));
  }

  @Test
  void testInsert() {
    tree.insert(5, 20);
    NoneLeafPage rootPage = (NoneLeafPage) this.tree.pageCache.getPage(0, false);
    LeafPage leafPage = (LeafPage) this.tree.pageCache.getPage(19, true);
    assertEquals(19, rootPage.getChildPageId(5));
    assertEquals(20, leafPage.getValue(5));
    assertEquals(20, tree.read(5));
  }


}
