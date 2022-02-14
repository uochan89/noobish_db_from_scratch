package page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import index.BTree;
import index.FileStorage;

class NoneLeafPageTest {

  private BTree tree = null;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {
    tree = BTree.getBTree("test_noneleafpage");
  }

  @AfterEach
  void tearDown() throws Exception {
    FileStorage.deleteBTree("test_noneleafpage");
  }

  @Test
  void testBinaryIO() {
    // noneleafpage は分割でしか発生しないからなー
    NoneLeafPage page = new NoneLeafPage();
    int key = 3;
    int value = 5;
    page.insert(key, value);
    tree.pageCache.assignNewPage(page);

    // wait for data to be persisted on storage
    try {
      System.out.println("wait 5 sec");
      Thread.sleep(5000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }

    NoneLeafPage pageFromStorage = null;
    pageFromStorage = (NoneLeafPage) tree.pageCache.getPage(0, false);

    int childPageID = pageFromStorage.getChildPageId(3);
    assertEquals(value, childPageID);
  }

}
