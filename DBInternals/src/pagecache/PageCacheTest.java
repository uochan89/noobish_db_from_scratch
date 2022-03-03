/**
 * 
 */
package pagecache;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import index.BTree;
import index.FileStorage;
import page.NoneLeafPage;

/**
 * @author yo-castle
 *
 */
class PageCacheTest {

  static BTree btree = null;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {
    btree = BTree.getBTree("page_cache_test");
  }


  @AfterAll
  static void tearDownAfterClass() throws Exception {
    FileStorage.deleteBTree("page_cache_test");
  }


  @BeforeEach
  void setUp() throws Exception {}


  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testGetPage() {
    int pageID = 0;
    String indexName = "page_cache_test";
    this.btree.pageCache.getPage(pageID);
    PageCacheKey<String, Integer> key = new PageCacheKey<String, Integer>(indexName, pageID);
    assertNotNull(PageCacheTest.btree.pageCache.cache.getIfPresent(key));
  }

  @Test
  void testPageUpdate() {
    int pageID = 0;
    NoneLeafPage page = (NoneLeafPage) PageCacheTest.btree.pageCache.getPage(pageID);
    page.insert(3, 10);
    page = null;
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    NoneLeafPage anotherPage = (NoneLeafPage) PageCacheTest.btree.pageCache.getPage(pageID);
    assertEquals(10, anotherPage.getChildPageId(3));

  }

}
