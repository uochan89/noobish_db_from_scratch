package index;

import java.util.HashMap;
import java.util.Map;

public class BTreeHeader {

  private static final int HEADER_SIZE = 200;
  public Map<Integer, Boolean> isLeaf = new HashMap<Integer, Boolean>();


  public boolean isLeafNode(int pageID) {
    Boolean isLeaf = this.isLeaf.get(pageID);
    if (isLeaf == null) {
      throw new IllegalStateException();
    }
    return isLeaf;
  }

}
