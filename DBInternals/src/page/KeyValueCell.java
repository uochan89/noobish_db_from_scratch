package page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import others.BinaryUtil;

public class KeyValueCell {

  private int key;
  private int value;

  private static final int KEY_SIZE_LENGTH = 4;
  private static final int VALUE_SIZE_LENGTH = 4;

  KeyValueCell(int key, int value) {
    this.key = key;
    this.value = value;
  }

  KeyValueCell(byte[] pageBinary, int cellOffset) {
    int i = cellOffset + 1;
    int key_size =
        BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary, i, i + KeyValueCell.KEY_SIZE_LENGTH));
    i += KeyValueCell.KEY_SIZE_LENGTH;
    int value_size = BinaryUtil
        .bytesToInt(Arrays.copyOfRange(pageBinary, i, i + KeyValueCell.VALUE_SIZE_LENGTH));
    i += KeyValueCell.VALUE_SIZE_LENGTH;
    this.key = BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary, i, i + key_size));
    i += key_size;
    this.value = BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary, i, i + value_size));
  }

  /**
   * key_size value_size key_bytes datarecord_bytes
   * 
   */
  public byte[] getBinary() {

    List<byte[]> data = new ArrayList<byte[]>();

    byte[] key_bytes = BinaryUtil.intToBytes(key);
    byte[] datarecord_bytes = BinaryUtil.intToBytes(value);
    byte[] key_size = BinaryUtil.intToBytes(key_bytes.length);
    byte[] value_size = BinaryUtil.intToBytes(datarecord_bytes.length);


    data.add(key_size);
    data.add(value_size);
    data.add(key_bytes);
    data.add(datarecord_bytes);

    byte[] binaryData = BinaryUtil.concatByteArrays(data);
    return binaryData;
  }

  public int getValue() {
    return this.value;
  }

  public Integer getKey() {
    return this.key;
  }


}
