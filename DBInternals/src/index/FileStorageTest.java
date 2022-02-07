package index;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

class FileStorageTest {

  @Test
  void testGetByteData() throws IOException {
    String filePath = "testdata/FileStorageTestDataTmp";
    int[] data = new int[] {23, 43, 11, 23123123};
    byte[] testdata = new byte[4];
    for (int i = 0; i < 4; i++) {
      testdata[i] = (byte) data[i];
    }
    Files.write(Paths.get(filePath), testdata);
    for (int i = 0; i < testdata.length; i++) {
      assertEquals(testdata[i], FileStorage.getByteData(filePath, i, i + 1)[0]);;
    }
  }

}
