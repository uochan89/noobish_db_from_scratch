package page;

import java.util.Arrays;
import org.apache.commons.lang3.SerializationUtils;

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
    int key_size = SerializationUtils
        .deserialize(Arrays.copyOfRange(pageBinary, 0, KeyValueCell.KEY_SIZE_LENGTH - 1));
    // this.pageId =
    // SerializationUtils.deserialize(Arrays.copyOfRange(pageBinary, KeyValueCell.KEY_SIZE_LENGTH,
    // KeyValueCell.VALUE_SIZE_LENGTH + KeyValueCell.KEY_SIZE_LENGTH - 2));
    this.key = SerializationUtils.deserialize(Arrays.copyOfRange(pageBinary,
        KeyValueCell.VALUE_SIZE_LENGTH + KeyValueCell.KEY_SIZE_LENGTH,
        KeyValueCell.VALUE_SIZE_LENGTH + KeyValueCell.KEY_SIZE_LENGTH + key_size - 1));
  }

  public byte[] getBinary() {
    /**
     * // convert to binary byte[] key = SerializationUtils.serialize(this.key); byte[] key_size =
     * SerializationUtils.serialize(key.length); byte[] value =
     * SerializationUtils.serialize(this.value); byte[] value_size =
     * SerializationUtils.serialize(value.length);
     * 
     * // apply zero-padding to fixed-size byte[] padded_key_size = Utils.applyZeroPadding(key_size,
     * KeyValueCell.KEY_SIZE_LENGTH); byte[] padded_value_size = Utils.applyZeroPadding(key_size,
     * KeyValueCell.VALUE_SIZE_LENGTH);
     **/

    byte[] padded_key_size = new byte[KeyValueCell.KEY_SIZE_LENGTH];
    byte[] padded_value_size = new byte[KeyValueCell.VALUE_SIZE_LENGTH];

    // append for cell binary
    // byte[] cellBinary =
    // new byte[KeyValueCell.KEY_SIZE_LENGTH + KeyValueCell.VALUE_SIZE_LENGTH + key.length];
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

    return cellBinary;
  }

  public int getValue() {
    return this.value;
  }

  public int getKey() {
    return this.key;
  }
}
