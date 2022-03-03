package page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NavigableSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import index.BTree;
import others.BinaryUtil;

public class NoneLeafPage extends Page {

  private Logger logger = LogManager.getLogger();
  // pageの解放ってどうする？
  // こういう風にpageの全ての値を読み込んでいるみたいだけども、修正するときは本当に必要なblockだけCoRでされるっていう認識であっているのかなあ
  private static final int PAGE_SIZE = 4000;
  private static final int OFFSET_SIZE = 4;
  private static final int HEADER_SIZE = 10;
  private PageHeader header;
  // offset, KeyValueCell
  private KeyValueCellMap keyValueCellMap;
  private List<Integer> offsets;
  private BTree btree;

  // TODO:freeになったセルとその大きさを記録する
  private int[][] availabilityList;
  // private Map<Integer, KeyValueCell> kvMap = new TreeMap<Integer, KeyValueCell>();

  public NoneLeafPage(BTree btree) {
    this.btree = btree;
    this.header = new PageHeader();
    this.header.isLeafPage = false;
    this.pageId = this.btree.assignPageId(this);
    this.keyValueCellMap =
        new KeyValueCellMap(NoneLeafPage.PAGE_SIZE - this.header.tmp_header_offset);
    this.offsets = new ArrayList<Integer>();
  }

  public NoneLeafPage(byte[] pageBinary, BTree btree) {
    // parse header
    this.header = new PageHeader(pageBinary);
    this.btree = btree;
    // tree map 使うのがよくね？ value でソートさせる必要がある？ 逆転すればいいだけ？ ー＞微妙。pointerのbeautyが見えにくくなる
    // parse offsets
    this.offsets = new ArrayList<Integer>();
    for (int i = 0; i < this.header.offsetCount; i++) {
      int t = this.header.tmp_header_offset;
      byte[] offset = Arrays.copyOfRange(pageBinary, t, t + NoneLeafPage.OFFSET_SIZE);
      this.offsets.add(BinaryUtil.bytesToInt(offset));
      t += NoneLeafPage.OFFSET_SIZE;
    }

    // parse cells
    this.keyValueCellMap =
        new KeyValueCellMap(NoneLeafPage.PAGE_SIZE - this.header.tmp_header_offset);
    for (int offset : this.offsets) {
      KeyValueCell cell = new KeyValueCell(pageBinary, offset);
      // TODO: how should i implement for not other programmers to use extended apis
      this.keyValueCellMap.add(cell);
    }
  }

  private List<Integer> getSortedOffset() {
    List<Integer> sortedOffsets = new ArrayList<Integer>();
    Integer key = this.keyValueCellMap.firstKey();
    //
    int offset_value = NoneLeafPage.PAGE_SIZE - 1;
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
    byte[] binary = new byte[NoneLeafPage.PAGE_SIZE];

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


  public int getChildPageId(int key) {
    // always connect to new pageID when searching down.
    if (this.header.offsetCount == 0) {
      logger.debug("No Child Node pageID : " + super.pageId);
      return -1;
    }

    // TODO:ここで新しいkeyが入る場所を探そうとするとnpになる
    // でもこうしてしまうと私い
    Integer lowerKey = this.keyValueCellMap.ceilingKey(key);
    if (lowerKey == null) {
      return this.header.getRightMostPageID();
    } else {
      return this.keyValueCellMap.get(lowerKey).getValue();
    }
  }

  public int createChildLeafNode(int key) {
    LeafPage leafPage = new LeafPage();
    int pageId = leafPage.pageId;
    KeyValueCell cell = new KeyValueCell(key, pageId);
    this.keyValueCellMap.put(key, cell);
    this.offsets.add(this.header.free_space_right_index - cell.getBinary().length);
    return pageId;
  }

  public int[] insert(int key, int pageID) {
    logger.debug("insert kv (" + key + " " + pageID + ") for pageID : " + super.pageId);

    KeyValueCell cell = new KeyValueCell(key, pageID);

    // check if new kv is insertable without splitting the page.
    boolean hasEnoughSpace = true;

    // update offset and celllist
    if (hasEnoughSpace) {
      this.header.cell_start_offset -= cell.getBinary().length;
      this.offsets.add(this.header.cell_start_offset);
      this.header.offsetCount += 1;
      Collections.sort(this.offsets);
      this.keyValueCellMap.add(cell);
      if (this.header.getRightMostPageID() == 0) {
        // これあってる？
        this.header.setRightMostPageID(this.btree.assignPageId(this));
      }
      // TODO : do i have to sava pageid to storage?
      logger.debug(
          "insert completed for kv (" + key + " " + pageID + ") for pageID : " + super.pageId);
      return new int[] {0};
    } else {
      int[] propatationInf = this.splitPage(key, pageID);
      return new int[] {-1, propatationInf[0], propatationInf[1]};
    }
  }

  private int[] splitPage(int key, int value) {
    NoneLeafPage newPage = new NoneLeafPage();

    NavigableSet<Integer> naviMap = this.keyValueCellMap.descendingKeySet();
    int propagatingKey = naviMap.first();
    int mapKey = propagatingKey;
    for (int i = 0; i < naviMap.size() / 2; i++) {
      newPage.insert(mapKey, this.keyValueCellMap.get(mapKey).getValue());
      mapKey = naviMap.higher(mapKey);
    }

    return new int[] {propagatingKey, newPage.pageId};

  }


}
