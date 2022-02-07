package index;

public class Utils {

  public static byte[] applyZeroPadding(byte[] value, int size) {
    if (value.length > size) {
      throw new IllegalArgumentException();
    }
    byte[] p = new byte[size];
    for (int i = 0; i < value.length; i++) {
      p[size - value.length + i] = value[i];
    }
    return p;
  }

  public static boolean isLeafNode(int pageId) {
    System.out.println(pageId);
    if (pageId == 1) {
      return false;
    } else {
      return true;
    }

  }
}
