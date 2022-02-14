package page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import others.BinaryUtil;

public class PageHeader {

  // offsets of fixed-sized variables
  private static final int PAGE_ID = 4;
  private static final int PAGE_SIZE = 4000;
  private static final int OFFSET_COUNT = 4;

  // header members
  public int pageID;
  public int offsetCount;

  // members that would be needed
  // {space_size, right_offset}
  int[][] availableList = new int[][] {{PAGE_SIZE - 10, PAGE_SIZE}};
  int tmp_header_offset = 4;
  int free_space_left_index = 400;
  int free_space_right_index = PAGE_SIZE;
  int cell_start_offset = 3999;
  boolean isLeafPage;
  int parentPageId;


  PageHeader() {}

  PageHeader(byte[] pageBinary) {
    this.offsetCount = BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary, 0, 4));
  }

  public byte[] getBinary() {

    List<byte[]> data = new ArrayList<byte[]>();

    byte[] offset_bytes = BinaryUtil.intToBytes(this.offsetCount);

    data.add(offset_bytes);


    byte[] binaryData = BinaryUtil.concatByteArrays(data);
    BinaryUtil.consoleOutByByte(binaryData);
    return binaryData;

  }

}
