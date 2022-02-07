package index;

import java.util.ArrayList;
import java.util.List;

public class BTreeHeader {

  private static final int HEADER_SIZE = 200;

  List<Boolean> isLeaf = new ArrayList<Boolean>();

  public BTreeHeader() {
    this.isLeaf.add(false);
    this.isLeaf.add(true);
  }

  public boolean isLeafNode(int pageID) {
    return this.isLeaf.get(pageID);
  }

}
