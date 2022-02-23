package page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import index.BTree;
import others.BinaryUtil;

class LeafPageTest {

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testBinaryIO() {
    BTree tree = BTree.getBTree("test_leafpage");

    LeafPage page = new LeafPage();
    page.insert(3, 5);
    tree.pageCache.assignNewPage(page);

    // wait for data to be persisted on storage
    try {
      System.out.println("wait 5 sec");
      Thread.sleep(5000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }

    System.out.println(page.pageId);
    LeafPage pageFromStorage = null;
    pageFromStorage = (LeafPage) tree.pageCache.getPage(page.pageId, true);

    int value = pageFromStorage.getValue(3);
    assertEquals(5, value);
  }

  @Test
  void insertWhithoutSplit() {
    LeafPage page = new LeafPage();
    page.insert(3, 5);
    BinaryUtil.consoleOutByByte(page.getBinary());

  }

  @Test
  void testInsertValues() {
    LeafPage page = new LeafPage();
    page.insert(3, 5);
    page.insert(10, 20);
    assertEquals(5, page.getValue(3));
    assertEquals(20, page.getValue(10));
  }

}
