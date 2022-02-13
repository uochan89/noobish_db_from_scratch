package page;

import java.nio.ByteBuffer;
import others.BinaryUtil;

public class Test {

  public static void main(String[] args) {
    Integer a = 3;

    byte[] v = Test.intToBytes(a);
    System.out.println(v);
    System.out.println(BinaryUtil.bytesToHex(v));
  }

  public static byte[] intToBytes(final int i) {
    ByteBuffer bb = ByteBuffer.allocate(4);
    bb.putInt(i);
    return bb.array();
  }
}
