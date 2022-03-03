package index;

import java.util.ArrayList;
import java.util.List;

public class BTreeTailer {

  private List<Integer> freePageIDs = new ArrayList<>();
  private int maxPageID = 0;

  public int assignNewPageID() {
    if (this.freePageIDs.size() > 0) {
      int v = this.freePageIDs.get((this.freePageIDs.size() - 1));
      this.freePageIDs.remove((this.freePageIDs.size() - 1));
      return v;
    } else {
      this.maxPageID++;
      return maxPageID;
    }
  }

}
