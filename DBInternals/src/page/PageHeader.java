package page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import others.BinaryUtil;

public class PageHeader {

  // offsets of fixed-sized variables
  private static final int PAGE_SIZE = 4000;
  private static final int OFFSET_COUNT = 4;

  // header members
  // public int pageID;
  public int offsetCount;

  // members that would be needed
  // {space_size, right_offset}
  int[][] availableList = new int[][] {{PAGE_SIZE - 10, PAGE_SIZE}};
  int tmp_header_offset = 12;
  int free_space_left_index = 400;
  int free_space_right_index = PAGE_SIZE;
  int cell_start_offset = 3999;
  Boolean isLeafPage = null;
  int parentPageId;

  private int rightMostPageID = 0;


  PageHeader() {}

  PageHeader(byte[] pageBinary) {
    this.offsetCount = BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary, 0, 4));
    this.rightMostPageID = BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary, 4, 8));
    this.isLeafPage =
        BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary, 8, 12)) == 1 ? true : false;
  }

  public byte[] getBinary() {
    // TODO: add RightMostPageID

    List<byte[]> data = new ArrayList<byte[]>();

    byte[] offset_bytes = BinaryUtil.intToBytes(this.offsetCount);
    byte[] rightMostPageID_bytes = BinaryUtil.intToBytes(this.rightMostPageID);
    byte[] isLeafPage_bytes = BinaryUtil.intToBytes(this.isLeafPage ? 1 : 0);

    data.add(offset_bytes);
    data.add(rightMostPageID_bytes);
    data.add(isLeafPage_bytes);


    byte[] binaryData = BinaryUtil.concatByteArrays(data);
    return binaryData;

  }

  public int getRightMostPageID() {
    return rightMostPageID;
  }

  public void setRightMostPageID(int rightMostPageID) {
    this.rightMostPageID = rightMostPageID;
  }


}
