package page;

import java.util.NavigableSet;
import java.util.SortedMap;
import java.util.TreeMap;

@SuppressWarnings("serial")
// TODO:こういうことしていいんだっけjavaAPIの本を見ればわかる。既存ライブラリの拡張のところ
public class KeyValueCellMap {

  private static final int OFFSET_SIZE = 4;

  private TreeMap<Integer, KeyValueCell> keyCellMap;
  // free space includes offset space
  private int free_space;


  public KeyValueCellMap(int free_space) {
    this.keyCellMap = new TreeMap<Integer, KeyValueCell>();
    this.free_space = free_space;
  }

  public void add(int key, int value) {
    if (!this.hasEnoughSpace(key, value)) {
      throw new IllegalStateException();
    }
    KeyValueCell cell = new KeyValueCell(key, value);
    this.add(cell);
  }

  public void add(KeyValueCell cell) {
    if (cell.getBinary().length > free_space) {
      throw new IllegalStateException();
    }
    this.keyCellMap.put(cell.getKey(), cell);
    this.free_space -= OFFSET_SIZE;
    this.free_space -= cell.getBinary().length;
  }

  public void remove(int key) {
    KeyValueCell cell = this.keyCellMap.get(key);
    this.free_space += OFFSET_SIZE;
    this.free_space -= cell.getBinary().length;
    this.keyCellMap.remove(key);
  }

  public NavigableSet<Integer> descendingKeySet() {
    return this.keyCellMap.descendingKeySet();
  }

  public boolean hasEnoughSpace(int key, int value) {
    KeyValueCell cell = new KeyValueCell(key, value);
    return cell.getBinary().length < free_space;
  }

  public KeyValueCellMap subMap(int keyFrom, int keyTo) {
    SortedMap<Integer, KeyValueCell> treeMap = this.keyCellMap.subMap(keyFrom, keyTo);
    KeyValueCellMap kvMap = new KeyValueCellMap(3996);
    this.keyCellMap = (TreeMap<Integer, KeyValueCell>) treeMap;
    return kvMap;
  }


  public Integer firstKey() {
    return this.keyCellMap.firstKey();
  }

  public Integer higherKey(Integer key) {
    return this.keyCellMap.higherKey(key);
  }

  public KeyValueCell get(Integer key) {
    return this.keyCellMap.get(key);
  }

  public Integer ceilingKey(int key) {
    return this.keyCellMap.ceilingKey(key);
  }


}
