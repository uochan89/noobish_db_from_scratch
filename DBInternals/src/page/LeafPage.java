package page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import index.BTree;
import others.BinaryUtil;

public class LeafPage extends Page {

  /**
   * structure header, offsets, pages, tail
   */

  private static final int PAGE_SIZE = 4000;
  private static final int OFFSET_SIZE = 4;

  private BTree btree;
  private Map<Integer, KeyValueCell> kvMap = new TreeMap<Integer, KeyValueCell>();
  private byte[] pageBinary;
  private PageHeader header;
  private TreeMap<Integer, KeyValueCell> keyValueCellMap;
  private List<Integer> offsets;
  // freeになったセルとその大きさを記録する
  private int[][] availabilityList;

  public LeafPage() {
    this.header = new PageHeader();
    super.pageId = BTree.assignPageId();
    this.keyValueCellMap = new TreeMap<Integer, KeyValueCell>();
    this.offsets = new ArrayList<Integer>();
  }

  // constructor for splitting
  public LeafPage(Map<Integer, KeyValueCell> kvMap) {
    this.kvMap = kvMap;
    // TODO: initialize other stuff
  }

  public LeafPage(byte[] pageBinary) {
    // parse header
    this.header = new PageHeader(pageBinary);

    // tree map 使うのがよくね？ value でソートさせる必要がある？ 逆転すればいいだけ？ ー＞微妙。pointerのbeautyが見えにくくなる
    // parse offsets
    this.offsets = new ArrayList<Integer>();
    for (int i = 0; i < this.header.offset_count; i++) {
      int t = this.header.tmp_header_offset;
      byte[] offset = Arrays.copyOfRange(pageBinary, t, t + LeafPage.OFFSET_SIZE);
      this.offsets.add(BinaryUtil.bytesToInt(offset));
      t += LeafPage.OFFSET_SIZE;
    }
    System.out.println(this.offsets);

    // parse cells
    this.keyValueCellMap = new TreeMap<Integer, KeyValueCell>();
    for (int offset : this.offsets) {
      KeyValueCell cell = new KeyValueCell(pageBinary, offset);
      this.keyValueCellMap.put(cell.getKey(), cell);
    }
  }

  public void propagateSplits() {
    // p.40を参考に実装する
    int propagatingKey = offsets.get(offsets.size() / 2);

    List<KeyValueCell> rightKeyValueCells = new ArrayList<KeyValueCell>();
    for (int i = 0; i < PAGE_SIZE - propagatingKey; i++) {
      rightKeyValueCells.add(this.keyValueCellMap.get(i + propagatingKey));
      this.keyValueCellMap.remove(i + propagatingKey);
    }
    // この操作でバグは発生しないのか？
    LeafPage rightPage = new LeafPage(keyValueCellMap);

    // 新しいキーの左側に対してもともとあった葉を紐づける
    NoneLeafPage parentPage = new NoneLeafPage(this.header.parentPageId);
    parentPage.insert(propagatingKey, this.pageId);

    // 新しいキーの右側に、右側の葉を紐づける


  }

  // It needs to be confirmed that this page is leaf node beforehand.
  // connect pointer of this object to parent none leaf node.
  public int[] insert(int key, int value) {
    KeyValueCell cell = new KeyValueCell(key, value);

    // check if new kv is insertable without splitting the page.
    boolean hasEnoughSpace = true;

    // update offset and celllist
    if (hasEnoughSpace) {
      this.header.cell_start_offset -= cell.getBinary().length;
      this.offsets.add(this.header.cell_start_offset);
      Collections.sort(this.offsets);
      this.keyValueCellMap.put(key, cell);
      return new int[] {0};
    } else {
      int propatationKey = this.splitPage(key, value);
      return new int[] {-1, propatationKey};
    }
  }

  private int splitPage(int key, int value) {
    Map<Integer, KeyValueCell> newKvMap = new TreeMap<Integer, KeyValueCell>();
    List<Integer> keySet = new ArrayList<Integer>(this.kvMap.keySet());
    for (int i = 0; i < keySet.size() / 2; i++) {
      int j = i + keySet.size() / 2;
      newKvMap.put(keySet.get(j), this.kvMap.get(keySet.get(j)));
    }

    LeafPage newPage = new LeafPage(newKvMap);

    return keySet.get(keySet.size() / 2);
  }

  // TODO:insertした時にoffsetのソートが保てていないので２分探索ができていない
  public int getValue(int key) {
    return this.keyValueCellMap.get(key).getValue();
  }


  private List<Integer> getSortedOffset() {
    List<Integer> sortedOffsets = new ArrayList<Integer>();
    Integer key = this.keyValueCellMap.firstKey();
    //
    int offset_value = LeafPage.PAGE_SIZE - 1;
    while (true) {
      KeyValueCell cell = this.keyValueCellMap.get(key);
      offset_value -= cell.getBinary().length;
      sortedOffsets.add(offset_value);
      // get next key
      key = this.keyValueCellMap.higherKey(key);
      if (key == null) {
        break;
      }
    }
    return sortedOffsets;
  }

  // TODO:might be more efficient to modify binary data on memoery each time its component value is
  // modified
  public byte[] getBinary() {
    byte[] binary = new byte[LeafPage.PAGE_SIZE];

    // add header
    byte[] headerBinary = this.header.getBinary();
    for (int i = 0; i < this.header.tmp_header_offset; i++) {
      binary[i] = headerBinary[i];
    }

    // add offsets
    int i_offset = this.header.tmp_header_offset;
    List<Integer> sortedOffsets = this.getSortedOffset();
    for (int offset : sortedOffsets) {
      System.out.println(offset);
      byte[] b = BinaryUtil.intToBytes(offset);
      for (int i = 0; i < b.length; i++) {
        binary[i_offset + i] = b[i];
      }
      i_offset += b.length;
    }
    // take all kv
    // sort key
    // caluculate offset of page set from fist


    // add keyValue
    int i_cell = this.header.free_space_right_index - 1;
    Integer key = this.keyValueCellMap.firstKey();
    while (true) {
      KeyValueCell cell = this.keyValueCellMap.get(key);
      byte[] cellBinary = cell.getBinary();
      int t = i_cell - cellBinary.length + 1;
      for (byte b : cellBinary) {
        binary[t] = b;
        t += 1;
      }
      i_cell -= cellBinary.length;

      // get next key
      key = this.keyValueCellMap.higherKey(key);
      if (key == null) {
        break;
      }
    }

    return binary;
  }



}
