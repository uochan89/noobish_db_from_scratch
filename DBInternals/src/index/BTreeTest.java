package index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BTreeTest {

  private static final String TREE_NAME = "BTreeTest";
  private BTree tree = null;


  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {
    tree = BTree.getBTree(TREE_NAME);
  }

  @AfterEach
  void tearDown() throws Exception {
    FileStorage.deleteBTree(TREE_NAME);
  }

  @Test
  void testSingleInsertAndRead() {
    tree.insert(11, 7);
    assertEquals(7, tree.read(11));
  }

  @Test
  void testMultipleInsertAndRead() {
    Random rand = new Random();
    // generate keys
    List<Integer> keys = new ArrayList<Integer>();
    for (int i = 0; i < 100; i++) {
      keys.add(i);
    }
    Collections.shuffle(keys);

    for (int i = 0; i < 100; i++) {
      int key = keys.get(i);
      int value = rand.nextInt((int) Math.pow(2, 31));
      System.out.println("testing kv : " + key + ", " + value);
      tree.insert(key, value);
      assertEquals(value, tree.read(key));
      System.out.println("asert ok");
    }

  }

  @Test
  void testInsertAndReadWithIO() {
    // ストレージからのページの読み取り、更新、保存の保存の部分がうまくいかない
    // readする時にストレージから読みとれれているので消されてはいるはず
    // 参照の問題かな。オブジェクト更新しているけど、参照が別みたいな
    // 普通キャッシュは何か重い演算の結果だから、編集なんてしないよね・・・どうなんやろ
    tree.insert(5, 20);
    assertEquals(20, tree.read(5));
    System.out.println("wait 5 sec");
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    tree = null;
    tree = BTree.getBTree(TREE_NAME);
    // assertEquals(20, tree.read(5));
  }


}
