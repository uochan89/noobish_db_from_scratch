package page;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class NoneLeafPage extends Page {

  // pageの解放ってどうする？
  // こういう風にpageの全ての値を読み込んでいるみたいだけども、修正するときは本当に必要なblockだけCoRでされるっていう認識であっているのかなあ
  private static final int PAGE_SIZE = 4000;
  private static final int OFFSET_SIZE = 2;
  private static final int HEADER_SIZE = 10;
  private PageHeader header;
  public int pageId;
  private Map<Integer, KeyCell> keyCellMap;
  private List<Integer> offsets;

  // TODO:freeになったセルとその大きさを記録する
  private int[][] availabilityList;
  private Map<Integer, KeyCell> kvMap = new TreeMap<Integer, KeyCell>();

  // constructor for splitting
  public NoneLeafPage(Map<Integer, KeyCell> kvMap) {
    this.kvMap = kvMap;
    // TODO: initialize other stuff
  }

  public NoneLeafPage(byte[] pageBinary) {

    // parse header
    // 固定長だけど、実際どこまで埋まっているかはheaderに確認してもらう
    this.header = new PageHeader(pageBinary);

    // tree map 使うのがよくね？ value でソートさせる必要がある？ 逆転すればいいだけ？ ー＞微妙。pointerのbeautyが見えにくくなる
    // parse offsets
    this.offsets = new ArrayList<Integer>();
    int t = HEADER_SIZE;
    for (int i = 0; i < header.offset_count; i++) {
      byte[] offset = new byte[NoneLeafPage.OFFSET_SIZE];
      for (int j = t; j < t + NoneLeafPage.OFFSET_SIZE; j++) {
        offset[j - t] = pageBinary[j];
        offsets.add(ByteBuffer.wrap(offset).getInt());
      }
      t += NoneLeafPage.OFFSET_SIZE;
    }

    // parse cells
    this.keyCellMap = new HashMap<Integer, KeyCell>();
    for (int offset : this.offsets) {
      KeyCell cell = new KeyCell(pageBinary, offset);
      this.keyCellMap.put(offset, cell);
    }
  }

  public byte[] getBinary() {
    byte[] page = new byte[100];
    return page;
  }


  public int getChildPageId(int key) {
    // always connect to new pageID when searching down.
    if (this.offsets.size() == 0) {
      return -1;
    }
    int ok = 0;
    int ng = this.offsets.size();

    while (Math.abs(ok - ng) > 1) {
      int mid = (ok + ng) / 2;
      KeyCell midCell = this.keyCellMap.get(this.offsets.get(mid));
      int midKey = midCell.getKey();
      if (midKey < key) {
        ok = mid;
      } else {
        ng = mid;
      }
    }
    return this.keyCellMap.get(this.offsets.get(ok)).getPageId();
  }

  public int createChildLeafNode(int key) {
    LeafPage leafPage = new LeafPage();
    int pageId = leafPage.pageId;
    KeyCell cell = new KeyCell(key, pageId);
    this.keyCellMap.put(key, cell);
    this.offsets.add(this.header.free_space_right_index - cell.getBinary().length);
    return pageId;
  }

  public int[] insert(int key, int pageID) {
    KeyCell cell = new KeyCell(key, pageID);

    // check if new kv is insertable without splitting the page.
    boolean hasEnoughSpace = true;

    // update offset and celllist
    if (hasEnoughSpace) {
      this.header.cell_start_offset -= cell.getBinary().length;
      this.offsets.add(this.header.cell_start_offset);
      Collections.sort(this.offsets);
      this.keyCellMap.put(key, cell);
      return new int[] {0};
    } else {
      int propatationKey = this.splitPage(key, pageID);
      return new int[] {-1, propatationKey};
    }
  }

  private int splitPage(int key, int value) {
    Map<Integer, KeyCell> newKvMap = new TreeMap<Integer, KeyCell>();
    List<Integer> keySet = new ArrayList<Integer>(this.kvMap.keySet());
    for (int i = 0; i < keySet.size() / 2; i++) {
      int j = i + keySet.size() / 2;
      newKvMap.put(keySet.get(j), this.kvMap.get(keySet.get(j)));
    }

    NoneLeafPage newPage = new NoneLeafPage(newKvMap);

    return keySet.get(keySet.size() / 2);
  }


}
