package page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import index.BTree;
import index.FileStorage;

class LeafPageTest {

  BTree tree;
  private static final String TREE_NAME = "test_leafpage";

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
  void testBinaryIO() {

    LeafPage page = new LeafPage(tree);
    page.insert(3, 5);
    tree.pageCache.assignNewPage(page);

    // wait for data to be persisted on storage
    try {
      System.out.println("wait 5 sec");
      Thread.sleep(5000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }

    LeafPage pageFromStorage = null;
    pageFromStorage = (LeafPage) tree.pageCache.getPage(page.pageId);

    int value = pageFromStorage.getValue(3);
    assertEquals(5, value);
  }

  @Test
  void testInsertValues() {
    LeafPage page = new LeafPage(tree);
    page.insert(3, 5);
    page.insert(10, 20);
    assertEquals(5, page.getValue(3));
    assertEquals(20, page.getValue(10));
  }

}
