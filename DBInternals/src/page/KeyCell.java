package page;

import java.util.Arrays;
import org.apache.commons.lang3.SerializationUtils;

public class KeyCell extends Cell {

  private int key;
  private int pageId;

  private static final int KEY_SIZE_LENGTH = 4;
  private static final int PAGE_ID_LENGTH = 4;

  KeyCell(int key, int pageId) {
    this.key = key;
    this.pageId = pageId;
  }

  KeyCell(byte[] pageBinary, int offset) {
    byte[] cellBinary =
        Arrays.copyOfRange(pageBinary, offset, offset + KeyCell.KEY_SIZE_LENGTH - 1);
    int key_size = SerializationUtils
        .deserialize(Arrays.copyOfRange(pageBinary, 0, KeyCell.KEY_SIZE_LENGTH - 1));
    this.pageId = SerializationUtils.deserialize(Arrays.copyOfRange(pageBinary,
        KeyCell.KEY_SIZE_LENGTH, KeyCell.PAGE_ID_LENGTH + KeyCell.KEY_SIZE_LENGTH - 2));
    this.key = SerializationUtils.deserialize(
        Arrays.copyOfRange(pageBinary, KeyCell.PAGE_ID_LENGTH + KeyCell.KEY_SIZE_LENGTH,
            KeyCell.PAGE_ID_LENGTH + KeyCell.KEY_SIZE_LENGTH + key_size - 1));
  }

  public byte[] getBinary() {
    // convert to binary
    // byte[] key = SerializationUtils.serialize(this.key);
    // byte[] key_size = SerializationUtils.serialize(key.length);
    // byte[] pageId = SerializationUtils.serialize(this.pageId);

    byte key = (byte) this.key;
    byte pageId = (byte) this.pageId;

    // apply zero-padding to fixed-size
    // byte[] padded_key_size = Utils.applyZeroPadding(key_size, KeyCell.KEY_SIZE_LENGTH);
    // byte[] padded_pageId = Utils.applyZeroPadding(pageId, KeyCell.PAGE_ID_LENGTH);

    byte[] padded_key_size = new byte[KeyCell.KEY_SIZE_LENGTH];
    byte[] padded_pageId = new byte[KeyCell.PAGE_ID_LENGTH];

    padded_key_size[0] = key;
    padded_pageId[0] = pageId;

    // append for cell binary
    // byte[] cellBinary = new byte[KeyCell.KEY_SIZE_LENGTH + KeyCell.PAGE_ID_LENGTH + key.length];
    byte[] cellBinary = new byte[KeyCell.KEY_SIZE_LENGTH + KeyCell.PAGE_ID_LENGTH + 1];

    int i = 0;
    while (i < cellBinary.length) {
      for (byte b : padded_key_size) {
        cellBinary[i] = b;
        i++;
      }
      for (byte b : padded_pageId) {
        cellBinary[i] = b;
        i++;
      }
      cellBinary[i] = key;
      i++;
    }

    return cellBinary;
  }

  public int getKey() {
    return this.key;
  }

  public int getPageId() {
    return this.pageId;
  }

}
