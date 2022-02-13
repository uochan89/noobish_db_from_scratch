package page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import others.BinaryUtil;

public class KeyValueCell extends Cell {

  private int key;
  private int value;

  private static final int KEY_SIZE_LENGTH = 4;
  private static final int VALUE_SIZE_LENGTH = 4;

  KeyValueCell(int key, int value) {
    this.key = key;
    this.value = value;
  }

  KeyValueCell(byte[] pageBinary, int offset) {
    byte[] cellBinary =
        Arrays.copyOfRange(pageBinary, offset, offset + KeyValueCell.KEY_SIZE_LENGTH - 1);
    int key_size =
        BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary, 0, KeyValueCell.KEY_SIZE_LENGTH - 1));
    // this.pageId =
    // SerializationUtils.deserialize(Arrays.copyOfRange(pageBinary, KeyValueCell.KEY_SIZE_LENGTH,
    // KeyValueCell.VALUE_SIZE_LENGTH + KeyValueCell.KEY_SIZE_LENGTH - 2));
    this.key = BinaryUtil.bytesToInt(Arrays.copyOfRange(pageBinary,
        KeyValueCell.VALUE_SIZE_LENGTH + KeyValueCell.KEY_SIZE_LENGTH,
        KeyValueCell.VALUE_SIZE_LENGTH + KeyValueCell.KEY_SIZE_LENGTH + key_size - 1));
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

  public int getKey() {
    return this.key;
  }

  private void idk() {
    byte[] cellBinary = new byte[KeyValueCell.KEY_SIZE_LENGTH + KeyValueCell.VALUE_SIZE_LENGTH + 2];

    int i = 0;
    for (byte b : padded_key_size) {
      cellBinary[i] = b;
      i++;
    }
    for (byte b : padded_value_size) {
      cellBinary[i] = b;
      i++;
    }
    cellBinary[i] = (byte) key;
    i++;
    cellBinary[i] = (byte) value;
    i++;

  }
}
