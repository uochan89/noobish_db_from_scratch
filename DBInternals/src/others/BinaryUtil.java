package others;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

public class BinaryUtil {

  private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = HEX_ARRAY[v >>> 4];
      hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static byte[] intToBytes(final int i) {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.putInt(i);
    return bb.array();
  }

  public static byte[] concatByteArrays(List<byte[]> byteArray) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    for (byte[] d : byteArray) {
      try {
        outputStream.write(d);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return outputStream.toByteArray();

  }

  public static int bytesToInt(byte[] bytes) {
    return ByteBuffer.wrap(bytes).getInt();
    // ByteBuffer wrapped = ByteBuffer.wrap(bytes); // big-endian by default
    // short num = wrapped.getShort();
    // return num;
  }
}
