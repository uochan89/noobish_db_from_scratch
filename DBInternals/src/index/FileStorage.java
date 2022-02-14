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
  public static byte[] readFile(String fileName, int offset, int size) throws IOException {
    System.out.println("read from storage");
    System.out.println(fileName + " : " + offset + " : " + size);
    File file = new File(INDEX_DIR + "/" + fileName);
    byte[] readData = new byte[size];
    RandomAccessFile ranomAFile = new RandomAccessFile(file, "r");
    ranomAFile.seek(offset);
    ranomAFile.read(readData);
    ranomAFile.close();
    return readData;
  }

  public static void updateFile(String indexName, byte[] data, int offset) throws IOException {
    System.out.println("write to storage");
    System.out.println(indexName + " : " + offset);
    String filePath = FileStorage.INDEX_DIR + "/" + indexName;
    if (!Files.exists(Paths.get(filePath))) {
      // initializeBTree(indexName);
      Files.createFile(Paths.get(filePath));
    }

    File file = new File(filePath);
    RandomAccessFile ranomAFile = new RandomAccessFile(file, "rw");
    ranomAFile.seek(offset);
    ranomAFile.write(data);
    ranomAFile.close();
  }

  // 1ページ分しか作成できていない。てか0埋めの書き込み意味ある？もともと０じゃん？
  public static void initializeBTree(String indexName) {
    int PAGE_SIZE = 4000;
    byte[] data = new byte[PAGE_SIZE];
    try {
      Files.createFile(Paths.get(INDEX_DIR + "/" + indexName));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void deleteBTree(String filePath) {
    try {
      Files.delete(Paths.get(INDEX_DIR + "/" + filePath));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
