package page;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import index.BTree;

public class LeafPage extends Page {

  private static final int PAGE_SIZE = 4000;
  private static final int OFFSET_SIZE = 4;
  private static final int tmp_header_length = 10;

  private BTree btree;
  private Map<Integer, KeyValueCell> kvMap = new TreeMap<Integer, KeyValueCell>();
  private byte[] pageBinary;
  private PageHeader header;
  private Map<Integer, KeyValueCell> keyValueCellMap;
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
    for (int i = 0; i < header.offset_count; i++) {
      int t = tmp_header_length;
      byte[] offset = Arrays.copyOfRange(pageBinary, t, t + LeafPage.OFFSET_SIZE);
      this.offsets.add(ByteBuffer.wrap(offset).getInt());
      t += LeafPage.OFFSET_SIZE;
    }

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


  private byte[] getBinary() {
    // byte[] updatedBinary = this.pageBinary;
    byte[] updatedBinary = new byte[100];
    System.out.println(updatedBinary);
    int i = this.header.tmp_header_offset + 1;
    for (int offset : this.offsets) {
      updatedBinary[i] = (byte) offset;
    }

    byte[] new_bianry = new byte[LeafPage.PAGE_SIZE];

    int j = this.header.free_space_right_index;
    for (KeyValueCell cell : this.keyValueCellMap.values()) {
      byte[] cellBinary = cell.getBinary();
      for (byte b : cellBinary) {
        updatedBinary[i] = b;
        i += 1;
      }
    }
    return updatedBinary;
  }



}
