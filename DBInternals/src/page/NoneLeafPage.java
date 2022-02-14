package page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import index.BTree;
import others.BinaryUtil;

public class NoneLeafPage extends Page {

  // pageの解放ってどうする？
  // こういう風にpageの全ての値を読み込んでいるみたいだけども、修正するときは本当に必要なblockだけCoRでされるっていう認識であっているのかなあ
  private static final int PAGE_SIZE = 4000;
  private static final int OFFSET_SIZE = 4;
  private static final int HEADER_SIZE = 10;
  private PageHeader header;
  public int pageId;
  // offset, KeyValueCell
  private TreeMap<Integer, KeyValueCell> KeyValueCellMap;
  private List<Integer> offsets;

  // TODO:freeになったセルとその大きさを記録する
  private int[][] availabilityList;
  // private Map<Integer, KeyValueCell> kvMap = new TreeMap<Integer, KeyValueCell>();

  public NoneLeafPage() {
    this.header = new PageHeader();
    this.pageId = BTree.assignPageId();
    this.KeyValueCellMap = new TreeMap<Integer, KeyValueCell>();
    this.offsets = new ArrayList<Integer>();
  }

  public NoneLeafPage(byte[] pageBinary) {
    // parse header
    this.header = new PageHeader(pageBinary);

    // tree map 使うのがよくね？ value でソートさせる必要がある？ 逆転すればいいだけ？ ー＞微妙。pointerのbeautyが見えにくくなる
    // parse offsets
    System.out.println("offset count : " + this.header.offsetCount);
    this.offsets = new ArrayList<Integer>();
    for (int i = 0; i < this.header.offsetCount; i++) {
      int t = this.header.tmp_header_offset;
      byte[] offset = Arrays.copyOfRange(pageBinary, t, t + NoneLeafPage.OFFSET_SIZE);
      System.out.println("omega : " + BinaryUtil.bytesToInt(offset));
      this.offsets.add(BinaryUtil.bytesToInt(offset));
      t += NoneLeafPage.OFFSET_SIZE;
    }

    // parse cells
    this.KeyValueCellMap = new TreeMap<Integer, KeyValueCell>();
    for (int offset : this.offsets) {
      System.out.println("looping offsets : " + offset);
      KeyValueCell cell = new KeyValueCell(pageBinary, offset);
      this.KeyValueCellMap.put(cell.getKey(), cell);
    }
  }

  private List<Integer> getSortedOffset() {
    List<Integer> sortedOffsets = new ArrayList<Integer>();
    Integer key = this.KeyValueCellMap.firstKey();
    //
    int offset_value = NoneLeafPage.PAGE_SIZE - 1;
    while (true) {
      KeyValueCell cell = this.KeyValueCellMap.get(key);
      offset_value -= cell.getBinary().length;
      sortedOffsets.add(offset_value);
      // get next key
      key = this.KeyValueCellMap.higherKey(key);
      if (key == null) {
        break;
      }
    }
    return sortedOffsets;
  }

  // TODO:might be more efficient to modify binary data on memoery each time its component value is
  // modified
  public byte[] getBinary() {
    System.out.println("NoneLeafPage getBianry");
    byte[] binary = new byte[NoneLeafPage.PAGE_SIZE];

    // add header
    byte[] headerBinary = this.header.getBinary();
    System.out.println("size : " + headerBinary.length);
    System.out.println("kokomadeikuyo : " + this.header.tmp_header_offset);
    for (int i = 0; i < this.header.tmp_header_offset; i++) {
      System.out.println(i);
      binary[i] = headerBinary[i];
    }

    // add offsets
    int i_offset = this.header.tmp_header_offset;
    List<Integer> sortedOffsets = this.getSortedOffset();
    for (int offset : sortedOffsets) {
      byte[] b = BinaryUtil.intToBytes(offset);
      for (int i = 0; i < b.length; i++) {
        binary[i_offset + i] = b[i];
      }
      i_offset += b.length;
    }

    // add keyValue
    int i_cell = this.header.free_space_right_index - 1;
    Integer key = this.KeyValueCellMap.firstKey();
    while (true) {
      KeyValueCell cell = this.KeyValueCellMap.get(key);
      byte[] cellBinary = cell.getBinary();
      int t = i_cell - cellBinary.length + 1;
      for (byte b : cellBinary) {
        binary[t] = b;
        t += 1;
      }
      i_cell -= cellBinary.length;

      System.out.println("kv added to page ID : " + this.pageId);

      // get next key
      key = this.KeyValueCellMap.higherKey(key);
      if (key == null) {
        break;
      }
    }

    return binary;
  }


  public int getChildPageId(int key) {
    // always connect to new pageID when searching down.
    if (this.header.offsetCount == 0) {
      System.out.println("naidesu");
      return -1;
    }

    // TODO:ここで新しいkeyが入る場所を探そうとするとnpになる
    // でもこうしてしまうと私い
    Integer lowerKey = this.KeyValueCellMap.floorKey(key);
    if (lowerKey == null) {
      System.out.println("kokokana");
      return -1;
    } else {
      return this.KeyValueCellMap.get(lowerKey).getValue();
    }
  }

  public int createChildLeafNode(int key) {
    LeafPage leafPage = new LeafPage();
    int pageId = leafPage.pageId;
    KeyValueCell cell = new KeyValueCell(key, pageId);
    this.KeyValueCellMap.put(key, cell);
    this.offsets.add(this.header.free_space_right_index - cell.getBinary().length);
    return pageId;
  }

  public int[] insert(int key, int pageID) {

    KeyValueCell cell = new KeyValueCell(key, pageID);

    // check if new kv is insertable without splitting the page.
    boolean hasEnoughSpace = true;

    // update offset and celllist
    if (hasEnoughSpace) {
      this.header.cell_start_offset -= cell.getBinary().length;
      this.offsets.add(this.header.cell_start_offset);
      System.out.println("offset count : " + this.header.offsetCount);
      this.header.offsetCount += 1;
      Collections.sort(this.offsets);
      this.KeyValueCellMap.put(key, cell);
      System.out.println("NoneLeafPage insert");
      System.out.println(key);
      System.out.println(pageID);
      System.out.println("offset count : " + this.header.offsetCount);
      System.out.println("######");
      return new int[] {0};
    } else {
      int propatationKey = this.splitPage(key, pageID);
      return new int[] {-1, propatationKey};
    }
  }

  private int splitPage(int key, int value) {
    Map<Integer, KeyValueCell> newKvMap = new TreeMap<Integer, KeyValueCell>();
    List<Integer> keySet = new ArrayList<Integer>(this.KeyValueCellMap.keySet());
    for (int i = 0; i < keySet.size() / 2; i++) {
      int j = i + keySet.size() / 2;
      newKvMap.put(keySet.get(j), this.KeyValueCellMap.get(keySet.get(j)));
    }

    NoneLeafPage newPage = new NoneLeafPage(newKvMap);

    return keySet.get(keySet.size() / 2);
  }

}
