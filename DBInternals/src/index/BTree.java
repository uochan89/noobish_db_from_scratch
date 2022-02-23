package index;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Stack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import page.LeafPage;
import page.NoneLeafPage;
import pagecache.PageCache;

public class BTree {

  public static void main(String[] args) {
    BTree tree = BTree.getBTree("new_omega");
    tree.insert(11, 7);
    tree.insert(14, 9);
    System.out.println("sucess");
  }

  private static Logger logger = LogManager.getLogger();
  private static final int ROOT_PAGE_ID = 0;
  private static final int HEADER_SIZE = 200;
  private static final int PAGE_SIZE = 4000;

  public PageCache pageCache;
  private BTreeHeader header = new BTreeHeader();
  private String indexName;


  public static int getPageIdOffset(int pageID) {
    return BTree.HEADER_SIZE + BTree.PAGE_SIZE * pageID;
  }

  private static Stack<Integer> avaliablePageIds = new Stack<Integer>();

  static {
    for (int i = 1; i < 20; i++) {
      BTree.avaliablePageIds.add(i);
    }
  }

  // used to retrieve btree data from storage
  private BTree(String indexName, byte[] headerBianry, byte[] tailBinary) {
    this(indexName);
  }

  // create initial BTree object on memory if not exists in storage
  private BTree(String indexName) {
    // initialize members
    this.indexName = indexName;
    this.pageCache = new PageCache(indexName);
  };

  //
  public static BTree getBTree(String indexName) {
    if (BTree.indexExists(indexName)) {
      try {
        byte[] headerBinary = FileStorage.readFile(indexName, 0, 0);
        byte[] tailBinary = FileStorage.readFile(indexName, 0, 0);
        return new BTree(indexName, headerBinary, tailBinary);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return null;
    } else {
      BTree tree = new BTree(indexName);
      FileStorage.initializeBTree(indexName);
      return tree;
    }
  }

  public void insert(int key, int value) {
    // TODO:header, tailのためのオブジェクト生成。実際のファイルアクセスはpageの層でやるので、thがないままだと不要
    logger.info("insert (key, value) = " + "(" + key + ", " + value + ")");
    Stack<Integer> breadCrumbs = new Stack<Integer>();
    this.getLeafPageIDWithBreadCrumbs(key, breadCrumbs);


    int leafPageID = breadCrumbs.pop();
    if (leafPageID == -1) {
      // TODO: assign kv to leaf page herez
      LeafPage leafPage = new LeafPage();
      leafPage.insert(key, value);
      leafPageID = leafPage.pageId;
      this.pageCache.assignNewPage(leafPage);

      // propagate the pageID of new leaf page to parents
      int parentPageID = breadCrumbs.pop();
      NoneLeafPage targetPage = (NoneLeafPage) this.pageCache.getPage(parentPageID, false);
      // result {isSucceed, propagateKey}
      int[] result = targetPage.insert(key, leafPageID);

      while (result[0] != 0) {
        key = result[1];
        if (breadCrumbs.size() == 0) {
          System.out.println("have to split root");
        }
        int childPageID = parentPageID;
        parentPageID = breadCrumbs.pop();
        NoneLeafPage parentPage = (NoneLeafPage) this.pageCache.getPage(parentPageID, false);
        result = parentPage.insert(key, childPageID);
      }
    } else {
      LeafPage targetPage = (LeafPage) this.pageCache.getPage(leafPageID, true);
      // propagate the insertion of value to leaf node.
      // TODO: この辺はもう一度propagationのロジックを各員してロジックの組み分けが必要
      // result {isSucceed, propagateKey}
      int[] result = targetPage.insert(key, value);
      int parentPageID = targetPage.pageId;
      while (result[0] != 0) {
        key = result[1];
        if (breadCrumbs.size() == 0) {
          System.out.println("have to split root");
        }
        int childPageID = parentPageID;
        parentPageID = breadCrumbs.pop();
        NoneLeafPage parentPage = (NoneLeafPage) this.pageCache.getPage(parentPageID, false);
        result = parentPage.insert(key, childPageID);
      }
    }
    logger.info("finished insert (key, value) = " + "(" + key + ", " + value + ")");
  }

  public int read(int key) {
    logger.info("start read (key) = " + "(" + key + ")");
    int leafPageID = this.getLeafPage(key);
    System.out.println("leafPageID : " + leafPageID);
    LeafPage page = (LeafPage) this.pageCache.getPage(leafPageID, true);
    int value = page.getValue(key);
    logger.info("finish read (key, value) = " + "(" + key + ", " + value + ")");
    return value;
  }

  // TODO: refine erro handling
  public static byte[] getPageBinary(String indexName, int pageID) {
    int from = BTree.HEADER_SIZE + BTree.PAGE_SIZE * pageID;
    System.out.println("from : " + from);
    try {
      return FileStorage.readFile(indexName, from, BTree.PAGE_SIZE);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static int assignPageId() {
    // やはり、tree headerに使用済みのページを管理する必要がある？
    // 一旦メモリ上で実現しておく
    int pageID = avaliablePageIds.pop();
    logger.debug("new pageID is assigned : " + pageID);
    return pageID;
  }

  private int getLeafPage(int key) {
    int pageId = BTree.ROOT_PAGE_ID;
    int childPageId = 0;
    while (pageId != -1 && !this.isLeafNode(pageId)) {
      NoneLeafPage page = null;
      page = (NoneLeafPage) this.pageCache.getPage(pageId, false);
      childPageId = page.getChildPageId(key);
      pageId = childPageId;
    }
    return pageId;
  }

  private void getLeafPageIDWithBreadCrumbs(int key, Stack<Integer> breadCrumbs) {
    int pageId = BTree.ROOT_PAGE_ID;
    while (pageId != -1 && !this.isLeafNode(pageId)) {
      breadCrumbs.add(pageId);
      NoneLeafPage page = null;
      page = (NoneLeafPage) this.pageCache.getPage(pageId, false);
      pageId = page.getChildPageId(key);
    }
    breadCrumbs.add(pageId);
  }


  // pageでisleaf情報を確認すると余計な管理とI/Oの増加が予想されるので、btreeのheaderで対応する
  private boolean isLeafNode(int pageID) {
    return this.header.isLeafNode(pageID);
  }

  private static BTree readBTreeFromStorage(String indexName) {
    Path indexFilePath = Paths.get("storage/" + indexName);
    byte[] header = new byte[1000];
    byte[] tail = new byte[1000];
    return new BTree(indexName, header, tail);
  }


  // TODO:is it ok to use method before construction finish?
  private static void initializeBTree(String indexName) throws IOException {
    new BTree(indexName);
  }

  private byte[] getBinary() {
    return new byte[10];
  }

  private static boolean indexExists(String indexName) {
    return Files.exists(Paths.get("storage/index/" + indexName));
  }

}
