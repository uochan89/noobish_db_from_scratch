package pagecache;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import index.BTree;
import index.FileStorage;
import page.LeafPage;
import page.NoneLeafPage;
import page.Page;

public class PageCache {

  private static int PAGE_SIZE = 4000;
  private BTree btree;
  private String treeName;

  /*
   * What PageCache do. - It keeps cached page contents in memory. - It allows modifications to
   * on-disk pages to be buffered together and performed against their cached version. - When a
   * requested page isn't present in memory and there's enough space available for it, it is paged
   * in by the page cache, and its cached version is returned. - If an already cached page is
   * requested, its cached version is returned. - If there's not enough space available for the new
   * page, some other page is evicted and its contents are flushed to disk.
   */

  /*
   * Flushing of diry pages are usually done with Write Ahead Log for durability. To Start
   * implementation simple, what facility this cache class have to fulfill? Let's say we dont't have
   * to think noting about durability. Then, flughing has to be done whenever the page is evicted
   * and its diry. and when the system shuts down.
   */


  // transactionのために明示的にpageを保存する場合は、メモリ上のpageは修正済みで、ストレージとのsyncは普通に保存を実行すればいいか？ i think so.
  // 実際保存するタイミングすらまだ教科書的に理解できていません・・・・
  // その機能はpagechacheで行う
  private static Logger logger = LogManager.getLogger();
  public LoadingCache<PageCacheKey<String, Integer>, Page> cache = null;

  public PageCache(BTree btree) {
    this.btree = btree;
    this.treeName = this.btree.indexName;

    CacheLoader<PageCacheKey<String, Integer>, Page> loader =
        new CacheLoader<PageCacheKey<String, Integer>, Page>() {
          @Override
          public Page load(PageCacheKey<String, Integer> key) throws Exception {
            // pageIdに合致するページをストレージから取得する
            // cacheをtree毎に作成するかどうか。index間で共有するならkeyをpageID以外にしないといけない
            byte[] pageBinary = BTree.getPageBinary(key.key1, key.key2);
            Page cachingPage = null;
            if (Page.isLeafPage(pageBinary)) {
              cachingPage = new LeafPage(pageBinary, btree, key.key2);
            } else {
              cachingPage = new NoneLeafPage(pageBinary, btree, key.key2);
            }
            logger.debug("cached page from storage with pageID : " + key.key2);
            return cachingPage;
          }
        };

    RemovalListener<PageCacheKey<String, Integer>, Page> listener =
        new RemovalListener<PageCacheKey<String, Integer>, Page>() {
          @Override
          public void onRemoval(
              RemovalNotification<PageCacheKey<String, Integer>, Page> notification) {
            logger.debug("evacuate page of key : {treeName : " + notification.getKey().key1
                + ", pageID : " + notification.getKey().key2 + "}");
            String indexName = notification.getKey().key1;
            byte[] pageBinary = notification.getValue().getBinary();
            int pageID = notification.getKey().key2;
            if (pageID == 0) {
              System.out.println("omega");
            }
            try {
              FileStorage.updateFile(indexName, pageBinary, BTree.getPageIdOffset(pageID));
            } catch (IOException e) {
              e.printStackTrace();
            }
            logger.debug("evacuated page of pageID : " + pageID);
          }
        };

    cache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.SECONDS)
        .removalListener(listener).build(loader);
  }

  public void assignNewPage(Page page) {
    PageCacheKey<String, Integer> key =
        new PageCacheKey<String, Integer>(this.treeName, page.pageId);
    this.cache.put(key, page);
    logger.debug("assigned page to cache with key : {treeName : " + this.treeName
        + ", page.pageId : " + page.pageId + "}");

  }

  public void savePageToStorage(int pageID) {
    Page page = null;
    page = this.getPage(pageID);
    int pageOffset = BTree.getPageIdOffset(pageID);
    try {
      FileStorage.updateFile(this.treeName, page.getBinary(), pageOffset);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public Page getPage(int pageID) {
    PageCacheKey<String, Integer> key = new PageCacheKey<String, Integer>(this.treeName, pageID);
    logger.debug("getPage from PageCache with key : {" + key.key1 + ", " + key.key2 + "}");
    try {
      return this.cache.get(key);
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    /*
     * if (page != null) { return page; } else {
     * System.out.println("page NOT found on cache pageID : " + pageID); byte[] pageBinary = null;
     * try { pageBinary = FileStorage.getByteData(this.treeName, PageCache.PAGE_SIZE * pageID,
     * PageCache.PAGE_SIZE);
     * 
     * String[] key = new String[] {};
     * 
     * cache.put(key, value); System.out.println("successfully cached from storage"); } catch
     * (IOException e) { e.printStackTrace(); } if (isLeaf) { return new LeafPage(pageBinary); }
     * else { return new NoneLeafPage(pageBinary); } }
     */
    return null;
  }


};
