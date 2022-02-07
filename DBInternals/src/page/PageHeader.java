package page;

import java.util.Arrays;

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
  int tmp_header_offset = 10;
  int offset_count;
  int free_space_left_index = 400;
  int free_space_right_index;
  int cell_start_offset = 3999;
  boolean isLeafPage;
  int parentPageId;


  PageHeader() {}

  PageHeader(byte[] pageBinary) {
    int leftOffset = 0;
    byte[] pageIDByte = Arrays.copyOfRange(pageBinary, leftOffset, PageHeader.PAGE_ID);
    // this.pageID = Byte.toUnsignedInt(pageIDByte);
    leftOffset += PageHeader.PAGE_ID;
    // byte[] offsetCount = Arrays.copyOfRange(pageBinary, leftOffset, PageHeader.OFFSET_COUNT);
    // this.offsetCount = Byte.toUnsignedInt(offsetCount);
    leftOffset += PageHeader.OFFSET_COUNT;
  }



}
