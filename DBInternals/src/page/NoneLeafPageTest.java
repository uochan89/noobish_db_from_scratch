package page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.NavigableSet;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import index.BTree;
import index.FileStorage;

class NoneLeafPageTest {

  private BTree tree = null;

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {
    tree = BTree.getBTree("test_noneleafpage");
  }

  @AfterEach
  void tearDown() throws Exception {
    FileStorage.deleteBTree("test_noneleafpage");
  }

  @Test
  void testSplitAndDeleteHalfPage() {
    NoneLeafPage page = new NoneLeafPage(this.tree);
    int[] result = null;
    int whenSplitHappend = 0;
    for (int i = 0; i < 251; i++) {
      result = page.insert(i, i);
      if (result[0] == -1) {
        whenSplitHappend = i;
        break;
      }
    }

    int splittedCount = (whenSplitHappend + 1) / 2 - 1;
    assertEquals(splittedCount, page.keyValueCellMap.size() - 1);
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    NoneLeafPage splittedPage = (NoneLeafPage) this.tree.pageCache.getPage(result[2]);
    // check if this has only left half
    KeyValueCellMap kvMap = splittedPage.keyValueCellMap;
    // minus one because 200th value that caused propagation hasn't been inserted yet.

    assertEquals(splittedCount, kvMap.size());
    int propagatingKey = kvMap.firstKey();
    for (int i = 0; i < kvMap.size(); i++) {
      int value = kvMap.get(propagatingKey).getValue();
      assertEquals(i, propagatingKey);
      assertEquals(i, value);
      Integer lowerKey = kvMap.higherKey(propagatingKey);
      if (lowerKey == null) {
        continue;
      }
      propagatingKey = lowerKey;
    }

    // check if original page has only right half
    KeyValueCellMap oriKvMap = page.keyValueCellMap;
    NavigableSet<Integer> oriNaviMap = oriKvMap.descendingKeySet();
    assertEquals(whenSplitHappend - kvMap.size(), oriNaviMap.size());
    int oriPropagatingKey = oriNaviMap.first();
    for (int i = 0; i < oriNaviMap.size(); i++) {
      int value = oriKvMap.get(oriPropagatingKey).getValue();
      // the largest value is whenSplitHappend - 1
      assertEquals(whenSplitHappend - 1 - i, oriPropagatingKey);
      assertEquals(whenSplitHappend - 1 - i, value);
      if (oriNaviMap.higher(oriPropagatingKey) == null) {
        continue;
      }
      oriPropagatingKey = oriNaviMap.higher(oriPropagatingKey);
    }

  }

  @Test
  void testSplitAsRootPage() {
    NoneLeafPage rootPage = (NoneLeafPage) this.tree.pageCache.getPage(0);
    for (int i = 0; i < 190; i++) {
      if (rootPage.keyValueCellMap.hasEnoughSpace(i, i)) {
        rootPage.insert(i, i);
      } else {
        throw new IllegalStateException();
      }
    }
    int[] result = rootPage.splitAsRootPage();

    try {
      Thread.sleep(6000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    // check leftNode
    NoneLeafPage leftPage = (NoneLeafPage) this.tree.pageCache.getPage(result[0]);
    int leftKey = leftPage.keyValueCellMap.firstKey();
    for (int i = 0; i <= 94; i++) {
      assertEquals(i, leftKey);
      assertEquals(i, leftPage.keyValueCellMap.get(i).getValue());
      if (i == 94) {
        continue;
      }
      leftKey = leftPage.keyValueCellMap.higherKey(leftKey);
    }

    // check rightNode
    NoneLeafPage rightPage = (NoneLeafPage) this.tree.pageCache.getPage(result[1]);
    int rightKey = rightPage.keyValueCellMap.lastKey();
    for (int i = 189; i >= 95; i--) {
      assertEquals(i, rightKey);
      assertEquals(i, rightPage.keyValueCellMap.get(i).getValue());
      if (i == 95) {
        continue;
      }
      rightKey = rightPage.keyValueCellMap.lowerKey(rightKey);
    }

    // get rootPage from cache
    rootPage = null;
    rootPage = (NoneLeafPage) this.tree.pageCache.getPage(0);

    // check rootNode
    Integer firstKey = rootPage.keyValueCellMap.firstKey();
    assertEquals(leftPage.keyValueCellMap.lastKey(), firstKey);
    int firstValue = rootPage.getChildPageId(firstKey);
    assertEquals(leftPage.pageId, firstValue);

    Integer secondKey = rootPage.keyValueCellMap.higherKey(firstKey);
    assertEquals(rightPage.keyValueCellMap.lastKey(), secondKey);
    int secondValue = rootPage.getChildPageId(secondKey);
    assertEquals(rightPage.pageId, secondValue);


  }



}
