package pagecache;

public class PageCacheKey<K1, K2, K3> {



  public K1 key1;
  public K2 key2;
  public K3 key3;

  public PageCacheKey(K1 key1, K2 key2, K3 key3) {
    this.key1 = key1;
    this.key2 = key2;
    this.key3 = key3;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PageCacheKey key = (PageCacheKey) o;
    if (key1 != null ? !key1.equals(key.key1) : key.key1 != null) {
      return false;
    }

    if (key2 != null ? !key2.equals(key.key2) : key.key2 != null) {
      return false;
    }

    if (key3 != null ? !key3.equals(key.key3) : key.key3 != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = key1 != null ? key1.hashCode() : 0;
    result = 31 * result + (key2 != null ? key2.hashCode() : 0);
    result = 31 * result + (key3 != null ? key3.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "[" + key1 + ", " + key2 + ", " + key3 + "]";
  }


}
