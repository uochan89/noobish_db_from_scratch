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

  @Test
  void testSplitPage() {
    for (int i = 200; i > 0; i--) {
      tree.insert(i, i);
    }
//    try {
//      Thread.sleep(7000);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
    System.out.println("insert and wait finished");
    LeafPage originalLeafPage = (LeafPage) tree.pageCache.getPage(1);
    LeafPage newLeafPage = (LeafPage) tree.pageCache.getPage(3);
    NoneLeafPage root = (NoneLeafPage) tree.pageCache.getPage(0);
    for (int i = 200; i > 0; i--) {
        assertEquals(i, tree.read(i));
      }
    System.out.println(tree.read(199));

    // offsetの編集がされていないのが問題だと思われる
    // offsetは更新されていないが、readの時にはしようされていないので、今回のエラーには直接関係ないのでは？


  }

}
