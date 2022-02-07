package index;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileStorage {

  private static final String INDEX_DIR = "./storage/index";

  // private FileHeader header;

  // read byte from offset to offset+size from file
  public static byte[] getByteData(String fileName, int offset, int size) throws IOException {
    File file = new File(INDEX_DIR + "/" + fileName);
    byte[] readData = new byte[size];
    RandomAccessFile ranomAFile = new RandomAccessFile(file, "r");
    ranomAFile.seek(offset);
    ranomAFile.read(readData);
    ranomAFile.close();
    return readData;
  }

  public static void updateByteData(String indexName, byte[] data, int offset) throws IOException {

    String filePath = FileStorage.INDEX_DIR + "/" + indexName;
    if (!Files.exists(Paths.get(filePath))) {
      initializeBTree(filePath);
    }

    int size = data.length;

    byte[] original = Files.readAllBytes(Paths.get(filePath));
    // only if i have to apppend page. not updating page

    // original.length - 1 < offset
    if (true) {
      // append
      byte[] appended = new byte[original.length + data.length];
      for (int i = original.length; i < original.length + data.length; i++) {
        appended[i] = data[i - original.length];
      }
      Files.delete(Paths.get(filePath));
      Files.write(Paths.get(filePath), appended);
    } else {
      // update
      for (int i = offset; i < offset + size; i++) {
        original[i] = data[i - offset];
        Files.delete(Paths.get(filePath));
        Files.write(Paths.get(filePath), original);
      }
    }


  }

  public static void initializeBTree(String filePath) {
    int PAGE_SIZE = 32;
    byte[] data = new byte[PAGE_SIZE];
    try {
      Files.createFile(Paths.get(INDEX_DIR + "/" + filePath));
      Files.write(Paths.get(filePath), data);
      System.out.println("file created successfully");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void deleteBTree(String filePath) {
    try {
      Files.delete(Paths.get(INDEX_DIR + "/" + filePath));
      System.out.println("index file deleted successfully");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
