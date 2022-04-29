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
    // generate random params
    List<Integer> keys = new ArrayList<Integer>();
    for (int i = 0; i < 100; i++) {
      keys.add(i);
    }
    Collections.shuffle(keys);

    for (int i = 0; i < 100; i++) {
      int key = keys.get(i);
      tree.insert(key, key);
      assertEquals(key, tree.read(key));
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
  void testInsertAndReadWithMerge() {
    for (int key = 0; key < 2000; key++) {
      int value = key;
      tree.insert(key, value);
    }
    System.out.println("inserted 2000 data");
    for (int key = 0; key < 2000; key++) {
      assertEquals(key, tree.read(key));
    }
  }

  // dame
  @Test
  void testInsertAndReadWithOnlyLeafPageSplit() {
    for (int key = 5000; key >= 0; key--) {
      int value = key;
      tree.insert(key, value);
    }
    /**
     * try { Thread.sleep(6000); } catch (InterruptedException e) { e.printStackTrace(); }
     */
    for (int key = 0; key < 5000; key++) {
      assertEquals(key, tree.read(key));
    }
  }

  @Test
  void testInsertAndReadWithRootNoneLeafPageSplit() {
    for (int key = 20000; key >= 0; key--) {
      int value = key;
      tree.insert(key, value);
    }
    try {
        Thread.sleep(7000);
      } catch (InterruptedException e) {
        e.printStackTrace();
    }
    for (int key = 0; key <= 20000; key++) {
      assertEquals(key, tree.read(key));
    }
  }
  
  @Test
  void testInsertAndReadWithInternalNoneLeafPageSplit() {
    for (int key = 111000; key >= 0; key--) {
      int value = key;
      tree.insert(key, value);
    }
    try {
        Thread.sleep(7000);
      } catch (InterruptedException e) {
        e.printStackTrace();
    }
    for (int key = 0; key <= 111000; key++) {
      assertEquals(key, tree.read(key));
    }
  }
  
  //ランダムな数字を入れるケース
  @Test
  void testRandomInsertAndRead() {
	  System.out.println("test start");
	//create random parameter
	ArrayList<Integer> param = new ArrayList<Integer>();
    for(int i = 0 ; i <= 2000 ; i++) {
    	param.add(i);
    }
    Collections.shuffle(param);
    
    for(int v : param) {
    	tree.insert(v, v);
    }
    try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
    }
    for (int key = 0; key <= 2000; key++) {
      assertEquals(key, tree.read(key));
    }
  }
}
