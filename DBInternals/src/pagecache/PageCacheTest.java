/**
 * 
 */
package pagecache;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import index.BTree;

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
  static void tearDownAfterClass() throws Exception {}


  @BeforeEach
  void setUp() throws Exception {}


  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testGetPage() {
    int pageID = 0;
    String indexName = "page_cache_test";
    boolean isLeaf = false;
    this.btree.pageCache.getPage(pageID, isLeaf);
    PageCacheKey<String, Integer, Boolean> key =
        new PageCacheKey<String, Integer, Boolean>(indexName, pageID, isLeaf);
    assertNotNull(this.btree.pageCache.cache.getIfPresent(key));
  }

}
