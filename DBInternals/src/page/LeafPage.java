package page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import index.BTree;
import others.BinaryUtil;

public class LeafPage extends Page {

  /**
   * structure header, offsets, pages, tail
   */

  private static Logger logger = LogManager.getLogger();
  private static final int OFFSET_SIZE = 4;

  private BTree btree;
  private Map<Integer, KeyValueCell> kvMap = new TreeMap<Integer, KeyValueCell>();
  private byte[] pageBinary;
  private PageHeader header;
  private KeyValueCellMap keyValueCellMap;
  private List<Integer> offsets;

  public LeafPage(BTree btree) {
    this.btree = btree;
    this.header = new PageHeader();
    this.header.isLeafPage = true;
    super.pageId = this.btree.assignPageId(this);
    this.keyValueCellMap = new KeyValueCellMap(Page.PAGE_SIZE - this.header.tmp_header_offset);
    this.offsets = new ArrayList<Integer>();
    logger.debug("created a new LeafPage with pageID : " + super.pageId);
  }

  // constructor for splitting
  public LeafPage(Map<Integer, KeyValueCell> kvMap) {
    this.kvMap = kvMap;
    // TODO: initialize other stuff
  }

  public LeafPage(byte[] pageBinary, BTree btree) {
    // parse header
    this.header = new PageHeader(pageBinary);
    this.btree = btree;
    // tree map 使うのがよくね？ value でソートさせる必要がある？ 逆転すればいいだけ？ ー＞微妙。pointerのbeautyが見えにくくなる
    // parse offsets
    this.offsets = new ArrayList<Integer>();
    int t = this.header.tmp_header_offset;
    for (int i = 0; i < this.header.offsetCount; i++) {
      byte[] offset = Arrays.copyOfRange(pageBinary, t, t + LeafPage.OFFSET_SIZE);
      this.offsets.add(BinaryUtil.bytesToInt(offset));
      t += LeafPage.OFFSET_SIZE;
    }

    // parse cells
    this.keyValueCellMap = new KeyValueCellMap(Page.PAGE_SIZE - this.header.tmp_header_offset);
    for (int offset : this.offsets) {
      KeyValueCell cell = new KeyValueCell(pageBinary, offset);
      this.keyValueCellMap.add(cell);
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
    logger.debug("insert (key, value) = " + "(" + key + ", " + value + ") to LeafPage(pageID : "
        + super.pageId + ")");
    KeyValueCell cell = new KeyValueCell(key, value);

    // update offset and celllist
    if (this.keyValueCellMap.hasEnoughSpace(key, value)) {
      this.header.cell_start_offset -= cell.getBinary().length;
      this.offsets.add(this.header.cell_start_offset);
      this.header.offsetCount += 1;
      Collections.sort(this.offsets);
      this.keyValueCellMap.add(cell);
      logger.debug(
          "inserted (key, value) = " + "(" + key + ", " + value + ") to pageID : " + super.pageId);
      return new int[] {0};
    } else {
      logger.debug("no more space in page start splitting");
      int[] propagationInf = this.splitPage(key, value);
      logger.debug("splited page new partition key : " + propagationInf[0] + " new pageID :"
          + propagationInf[1]);
      return new int[] {-1, propagationInf[0], propagationInf[1]};
    }
  }

  private int[] splitPage(int key, int value) {
    LeafPage newPage = new LeafPage(this.btree);
    this.btree.pageCache.assignNewPage(newPage);
    newPage.insert(key, value);

    NavigableSet<Integer> naviMap = this.keyValueCellMap.descendingKeySet();
    int propagatingKey = naviMap.last();
    for (int i = 0; i < naviMap.size() / 2; i++) {
      newPage.insert(propagatingKey, this.keyValueCellMap.get(propagatingKey).getValue());
      propagatingKey = naviMap.lower(propagatingKey);
    }
    return new int[] {naviMap.higher(propagatingKey), newPage.pageId};
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
      KeyValueCell cell = (KeyValueCell) this.keyValueCellMap.get(key);
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
      KeyValueCell cell = (KeyValueCell) this.keyValueCellMap.get(key);
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

  @Override
  public int getChildPageId(int key) {
    return -1;
  }



}
