package page;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import index.BTree;

public class LeafPage2 extends Page {

  private static final int PAGE_SIZE = 4000;
  private static final int OFFSET_SIZE = 4;
  private static final int tmp_header_length = 10;

  private BTree btree;
  private byte[] pageBinary;
  private PageHeader header;
  public int pageId;
  private List<KeyValueCell> keyValueCells;
  private List<Integer> offsets;
  // freeになったセルとその大きさを記録する
  private int[][] availabilityList;

  public LeafPage2() {
    this.header = new PageHeader();
    this.pageId = BTree.assignPageId();
    this.keyValueCells = new ArrayList<KeyValueCell>();
    this.offsets = new ArrayList<Integer>();
  }


  public LeafPage2(byte[] pageBinary) {

    // parse header
    this.header = new PageHeader(pageBinary);

    // tree map 使うのがよくね？ value でソートさせる必要がある？ 逆転すればいいだけ？ ー＞微妙。pointerのbeautyが見えにくくなる
    // parse offsets
    this.offsets = new ArrayList<Integer>();
    for (int i = 0; i < header.offset_count; i++) {
      int t = tmp_header_length;
      byte[] offset = Arrays.copyOfRange(pageBinary, t, t + LeafPage2.OFFSET_SIZE);
      this.offsets.add(ByteBuffer.wrap(offset).getInt());
      t += LeafPage2.OFFSET_SIZE;
    }

    // parse cells
    this.keyValueCells = new ArrayList<KeyValueCell>();
    for (int offset : this.offsets) {
      KeyValueCell cell = new KeyValueCell(pageBinary, offset);
      this.keyValueCells.add(cell);
    }
  }

  public void propagateSplits() {
    // p.40を参考に実装する
    int propagatingKey = offsets.get(offsets.size() / 2);

    List<KeyValueCell> rightKeyValueCells = new ArrayList<KeyValueCell>();
    for (int i = 0; i < PAGE_SIZE - propagatingKey; i++) {
      rightKeyValueCells.add(this.keyValueCells.get(i + propagatingKey));
      this.keyValueCells.remove(i + propagatingKey);
    }
    // この操作でバグは発生しないのか？
    LeafPage2 rightPage = new LeafPage2(rightKeyValueCells);

    // 新しいキーの左側に対してもともとあった葉を紐づける
    NoneLeafPage parentPage = new NoneLeafPage(this.header.parentPageId);
    parentPage.insert(propagatingKey, this.pageId);

    // 新しいキーの右側に、右側の葉を紐づける


  }

  // It needs to be confirmed that this page is leaf node beforehand.
  // connect pointer of this object to parent none leaf node.
  public boolean insert(int key, int value) {
    KeyValueCell cell = new KeyValueCell(key, value);

    // check if new kv is insertable without splitting the page.
    boolean hasEnoughSpace = true;

    // update offset and celllist
    if (hasEnoughSpace) {
      this.header.cell_start_offset -= cell.getBinary().length;
      this.offsets.add(this.header.cell_start_offset);
      Collections.sort(this.offsets);
      keyValueCells.add(cell);
    } else {
      return this.splitPage(key, value);
    }
  }

  private int splitPage(int key, int value) {
    LeafPage2 newPage = new LeafPage2();
    for (int i = 0; i < this.offsets.size() / 2; i++) {
      newNode.values[i] = this.values[i + this.offsets.size() / 2];
      this.values[i] = 0;
    }
    newNode.lastItemIndex = OCCUPANCY / 2 - 1;
    this.lastItemIndex = OCCUPANCY / 2 - 1;

    int promoting_key = values[OCCUPANCY / 2];
    this.partentNode.insert(promoting_key, newNode);
  }

  public int getValue(int key) {
    int ok = 0;
    int ng = this.offsets.size();

    while (Math.abs(ok - ng) > 1) {
      int mid = (ok + ng) / 2;
      KeyValueCell midCell = this.keyValueCells.get(this.offsets.get(mid));
      int midKey = midCell.getKey();
      if (midKey < key) {
        ok = mid;
      } else {
        ng = mid;
      }
    }
    return this.keyValueCells.get(this.offsets.get(ok)).getValue();
  }


  private byte[] getBinary() {
    // byte[] updatedBinary = this.pageBinary;
    byte[] updatedBinary = new byte[100];
    System.out.println(updatedBinary);
    int i = this.header.tmp_header_offset + 1;
    for (int offset : this.offsets) {
      updatedBinary[i] = (byte) offset;
    }

    byte[] new_bianry = new byte[LeafPage2.PAGE_SIZE];

    int j = this.header.free_space_right_index;
    for (KeyValueCell cell : this.keyValueCells) {
      byte[] cellBinary = cell.getBinary();
      for (byte b : cellBinary) {
        updatedBinary[i] = b;
        i += 1;
      }
    }
    return updatedBinary;
  }



}
