package index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.nio.file.Files;
import java.nio.file.Paths;
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

  private static final String INDEX_DIR = "./storage/index";

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testSingleInsertAndRead() {
    BTree tree = BTree.getBTree("testBtree");
    tree.insert(11, 7);
    assertEquals(7, tree.read(11));
    FileStorage.deleteBTree("testBtree");
  }

  @Test
  void testMultipleInsertAndRead() {
    if (Files.exists(Paths.get(INDEX_DIR + "/" + "testdata_BTree"))) {
      FileStorage.deleteBTree("testdata_BTree");
    }
    BTree tree = BTree.getBTree("testdata_BTree");

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
      System.out.println("asert ok");
    }

    FileStorage.deleteBTree("testdata_BTree");
  }

}
