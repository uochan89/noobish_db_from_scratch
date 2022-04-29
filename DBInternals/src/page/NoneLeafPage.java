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
  public KeyValueCellMap keyValueCellMap;
  private List<Integer> offsets;
  private BTree btree;

  // TODO:freeになったセルとその大きさを記録する
  private int[][] availabilityList;
  // private Map<Integer, KeyValueCell> kvMap = new TreeMap<Integer, KeyValueCell>();

  public NoneLeafPage(BTree btree) {
    this.btree = btree;
    this.header = new PageHeader();
    this.header.isLeafPage = false;
    super.pageId = this.btree.assignPageId(this);
    // this.header.pageID = super.pageId;
    this.keyValueCellMap =
        new KeyValueCellMap(NoneLeafPage.PAGE_SIZE - this.header.tmp_header_offset);
    this.offsets = new ArrayList<Integer>();
    this.btree.pageCache.assignNewPage(this);
  }

  public NoneLeafPage(byte[] pageBinary, BTree btree, int pageID) {
    this.pageId = pageID;
    // parse header
    this.header = new PageHeader(pageBinary);
    this.btree = btree;
    // tree map 使うのがよくね？ value でソートさせる必要がある？ 逆転すればいいだけ？ ー＞微妙。pointerのbeautyが見えにくくなる
    // parse offsets
    this.offsets = new ArrayList<Integer>();
    int t = this.header.tmp_header_offset;
    for (int i = 0; i < this.header.offsetCount; i++) {
      byte[] offset = Arrays.copyOfRange(pageBinary, t, t + NoneLeafPage.OFFSET_SIZE);
      this.offsets.add(BinaryUtil.bytesToInt(offset));
      System.out.println(BinaryUtil.bytesToInt(offset));
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

  /**
   * 
   * @return KeyValueCellの昇順でのそのoffsetを並べたリストを返す
   */
  private List<Integer> getSortedOffset() {
    List<Integer> sortedOffsets = new ArrayList<Integer>();
    if (this.keyValueCellMap.size() != 0) {
      Integer key = this.keyValueCellMap.firstKey();
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
    }
    return sortedOffsets;
  }

  // TODO:might be more efficient to modify binary data on memoery each time its component value is
  // modified
  public byte[] getBinary() {
    byte[] binary = new byte[NoneLeafPage.PAGE_SIZE];

    // add header
    byte[] headerBinary = this.header.getBinary();
    for (int i = 0; i < headerBinary.length; i++) {
      binary[i] = headerBinary[i];
    }

    // add offsets
    int i_offset = this.header.tmp_header_offset;
    List<Integer> sortedOffsets = this.getSortedOffset();
    for (int offset : sortedOffsets) {
      // TODO can be a bug. thought offset is fixed size.
      byte[] b = BinaryUtil.intToBytes(offset);
      for (int i = 0; i < b.length; i++) {
        binary[i_offset + i] = b[i];
      }
      i_offset += b.length;
    }

    // add keyValue
    int cell_end_index = this.header.free_space_right_index - 1;
    if (this.keyValueCellMap.size() != 0) {
      Integer key = this.keyValueCellMap.firstKey();
      while (true) {
        KeyValueCell cell = this.keyValueCellMap.get(key);
        byte[] cellBinary = cell.getBinary();
        int cell_start_index = cell_end_index - cellBinary.length + 1;
        for (byte b : cellBinary) {
          binary[cell_start_index] = b;
          cell_start_index += 1;
        }
        cell_end_index -= cellBinary.length;

        // get next key
        key = this.keyValueCellMap.higherKey(key);
        if (key == null) {
          break;
        }
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

  public int[] insert(int key, int pageID) {
    logger.debug("insert kv (" + key + " " + pageID + ") for pageID : " + super.pageId);

    KeyValueCell cell = new KeyValueCell(key, pageID);

    // update offset and celllist
    if (this.keyValueCellMap.hasEnoughSpace(key, pageID)) {
      this.header.cell_start_offset -= cell.getBinary().length;
      this.offsets.add(this.header.cell_start_offset);
      this.header.offsetCount += 1;
      Collections.sort(this.offsets);
      this.keyValueCellMap.add(cell);
      //TODO: おそらくinsertによるrightmostpageIDの境界の変更とsplit時の対応が何も考えられていない
      if (this.header.getRightMostPageID() == 0) {
        this.header.setRightMostPageID(this.btree.assignPageId(this));
      }
      logger.debug(
          "insert completed for kv (" + key + " " + pageID + ") for pageID : " + super.pageId);
      return new int[] {0};
    } else {
      if(this.pageId == 0) {
    	  int[] newPageIDs = this.splitAsRootPage();
    	  NoneLeafPage leftHalfPage = (NoneLeafPage) this.btree.pageCache.getPage(newPageIDs[0]);
    	  leftHalfPage.insert(key, pageID);
    	  return new int[] {0};
      }else {
    	  logger.fatal("no more space in None Leaf Page start splitting");
    	  int[] propagationInf = this.splitAndDeleteHalfPage();
    	  NoneLeafPage newPage = (NoneLeafPage) this.btree.pageCache.getPage(propagationInf[1]);
    	  newPage.insert(key, pageID);
          logger.debug("splited page new partition key : " + propagationInf[0] + " new pageID :"
              + propagationInf[1]);
          return new int[] {-1, propagationInf[0], propagationInf[1]};
      }
    }
  }

  public int[] splitAsRootPage() {
    int[] prpgtInfLeft = this.splitLeftHalfPage();
    int[] prpgtInfRight = this.splitRightHalfPage();
    // NoneLeafPageを新規作成
    NoneLeafPage rootPage = new NoneLeafPage(this.btree);
    // pageID = 0としてcacheを更新
    rootPage.pageId = 0;
    rootPage.btree.pageCache.assignNewPage(rootPage);
    // root pageに上の情報を入れる。
    rootPage.insert(prpgtInfLeft[0], prpgtInfLeft[1]);
    rootPage.insert(prpgtInfRight[0], prpgtInfRight[1]);

    return new int[] {prpgtInfLeft[1], prpgtInfRight[1]};
  }

  public int[] splitAndDeleteHalfPage() {
    NoneLeafPage newPage = new NoneLeafPage(this.btree);

    int propagatingKey = this.keyValueCellMap.firstKey();
    int originalKVSize = this.keyValueCellMap.size();
    logger.debug("NoneLeafPage, splitAndDeleteHalfPage");
    int loop_size = originalKVSize / 2;
    for (int i = 0; i < originalKVSize / 2; i++) {
      newPage.insert(propagatingKey, this.keyValueCellMap.get(propagatingKey).getValue());
      
      //insert時と逆の同じ操作をする
      this.offsets.remove(this.offsets.indexOf(this.header.cell_start_offset));
      this.header.cell_start_offset += this.keyValueCellMap.get(propagatingKey).getBinary().length;
      this.header.offsetCount -= 1;
      this.keyValueCellMap.remove(propagatingKey);
      if (i != loop_size - 1) {
    	  propagatingKey = this.keyValueCellMap.higherKey(propagatingKey);
      }
    }
    return new int[] {propagatingKey, newPage.pageId};
  }

  private int[] splitLeftHalfPage() {
    NoneLeafPage newPage = new NoneLeafPage(this.btree);
    this.btree.pageCache.assignNewPage(newPage);

    NavigableSet<Integer> naviMap = this.keyValueCellMap.descendingKeySet();
    Integer propagatingKey = naviMap.last();
    for (int i = 0; i < naviMap.size() / 2; i++) {
      newPage.insert(propagatingKey, this.keyValueCellMap.get(propagatingKey).getValue());
      propagatingKey = naviMap.lower(propagatingKey);
    }
    return new int[] {naviMap.higher(propagatingKey), newPage.pageId};
  }

  private int[] splitRightHalfPage() {
    NoneLeafPage newPage = new NoneLeafPage(this.btree);
    this.btree.pageCache.assignNewPage(newPage);

    NavigableSet<Integer> naviMap = this.keyValueCellMap.descendingKeySet();
    int propagatingKey = naviMap.first();
    for (int i = naviMap.size() / 2; i < naviMap.size(); i++) {
      newPage.insert(propagatingKey, this.keyValueCellMap.get(propagatingKey).getValue());
      propagatingKey = naviMap.higher(propagatingKey);
    }
    return new int[] {this.keyValueCellMap.lastKey(), newPage.pageId};
  }



}
