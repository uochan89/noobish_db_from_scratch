package pagecache;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import index.BTree;
import index.FileStorage;
import page.LeafPage;
import page.NoneLeafPage;
import page.Page;

public class PageCache {

  private static int PAGE_SIZE = 4000;
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
  public LoadingCache<PageCacheKey<String, Integer, Boolean>, Page> cache = null;

  public PageCache(String treeName) {
    this.treeName = treeName;

    CacheLoader<PageCacheKey<String, Integer, Boolean>, Page> loader =
        new CacheLoader<PageCacheKey<String, Integer, Boolean>, Page>() {
          @Override
          public Page load(PageCacheKey<String, Integer, Boolean> key) throws Exception {
            // pageIdに合致するページをストレージから取得する
            // cacheをtree毎に作成するかどうか。index間で共有するならkeyをpageID以外にしないといけない
            System.out.println("cache data from storage");
            byte[] pageBinary = BTree.getPageBinary(key.key1, key.key2);
            if (key.key3) {
              return new LeafPage(pageBinary);
            } else {
              return new NoneLeafPage(pageBinary);
            }
          }
        };

    cache = CacheBuilder.newBuilder().maximumSize(10).build(loader);
  }

  public void assignNewPage(Page page) {
    PageCacheKey<String, Integer, Boolean> key = new PageCacheKey<String, Integer, Boolean>(
        this.treeName, page.pageId, page instanceof LeafPage);
    this.cache.put(key, page);
  }

  public void savePageToStorage(int pageID, boolean isLeaf) {
    Page page = null;
    page = this.getPage(pageID, isLeaf);
    int pageOffset = BTree.getPageIdOffset(pageID);
    try {
      FileStorage.updateByteData(this.treeName, page.getBinary(), pageOffset);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  public Page getPage(int pageID, boolean isLeaf) {
    PageCacheKey<String, Integer, Boolean> key =
        new PageCacheKey<String, Integer, Boolean>(this.treeName, pageID, isLeaf);
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