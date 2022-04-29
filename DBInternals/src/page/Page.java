package page;

public abstract class Page {

  protected static final int PAGE_SIZE = 4000;

  public int pageId;

  public abstract byte[] getBinary();

  public abstract int getChildPageId(int key);

  public static boolean isLeafPage(byte[] pageBinary) {
    return new PageHeader(pageBinary).isLeafPage;
  }
}
