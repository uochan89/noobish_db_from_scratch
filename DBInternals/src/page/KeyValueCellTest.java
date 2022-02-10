package page;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Arrays;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import others.BinaryUtil;

class KeyValueCellTest {

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {}

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testGetBinaryAndPageConstructor() {
    KeyValueCell cell = new KeyValueCell(1000, 216310283);
    byte[] binary = cell.getBinary();

    int i = 0;
    byte[] key_size = Arrays.copyOfRange(binary, i, i + 4);
    i += 4;
    byte[] value_size = Arrays.copyOfRange(binary, i, i + 4);
    i += 4;
    byte[] key = Arrays.copyOfRange(binary, i, i + BinaryUtil.bytesToInt(key_size));
    i += BinaryUtil.bytesToInt(key_size);
    byte[] value = Arrays.copyOfRange(binary, i, i + BinaryUtil.bytesToInt(value_size));

    assertEquals(cell.getKey(), BinaryUtil.bytesToInt(key));
    assertEquals(cell.getValue(), BinaryUtil.bytesToInt(value));
  }

}
