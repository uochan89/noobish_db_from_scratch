package index;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import others.BinaryUtil;

class FileStorageTest {

  private final static String FILE_NAME = "FileStorageTestDataTmp";
  private final static Path PATH = Paths.get("./storage/index/" + FILE_NAME);

  @BeforeAll
  static void setUpBeforeClass() throws Exception {}

  @AfterAll
  static void tearDownAfterClass() throws Exception {}

  @BeforeEach
  void setUp() throws Exception {
    if (Files.exists(PATH)) {
      Files.delete(PATH);
    }
  }

  @AfterEach
  void tearDown() throws Exception {}

  @Test
  void testGetByteData() throws IOException {
    int data = 23123;
    byte[] db = BinaryUtil.intToBytes(data);
    Files.write(PATH, db);
    System.out.println(db.length);
    byte[] persistedData = FileStorage.getByteData(FILE_NAME, 0, db.length);
    assertArrayEquals(db, persistedData);
  }

  @Test
  void testCreateFile() {
    byte[] data = new byte[] {12, 3, 123, 12};
    try {
      FileStorage.updateBtree(FILE_NAME, data, 0);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    byte[] persistedData = null;
    try {
      persistedData = FileStorage.getByteData(FILE_NAME, 0, 4000);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    for (int i = 0; i < data.length; i++) {
      assertEquals(data[i], persistedData[i]);
    }
  }

}
