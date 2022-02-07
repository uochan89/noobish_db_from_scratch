package page;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import pagecache.PageCacheKey;

public class Test {
  public static void main(String[] args) throws IOException {
    /*
     * CacheLoader<String[], String> loader; loader = new CacheLoader<String[], String>() {
     * 
     * @Override public String load(String[] key) { System.out.println("not found in cache"); return
     * "takashi"; } };
     * 
     * LoadingCache<String[], String> cache; cache = CacheBuilder.newBuilder().build(loader);
     * System.out.println(cache.stats()); cache.getUnchecked(new String[] {"omega", "omega"});
     * cache.getUnchecked(new String[] {"omega", "omega"}); cache.getUnchecked(new String[]
     * {"omega", "omega"}); System.out.println(cache.getIfPresent(cache.getUnchecked(new String[]
     * {"omega", "omega"}))); System.out.println(cache.stats());
     */

    String a = "hashcode";
    String b = new String("hashcode");
    System.out.println(a.hashCode());
    System.out.println(b.hashCode());


    String[] arrayA = new String[] {"omega", "omega"};
    String[] arrayB = new String[] {"omega", "omega"};
    System.out.println(arrayA.hashCode());
    System.out.println(arrayB.hashCode());


    // Create a `HashMap` with `Key` as key
    Map<PageCacheKey<String, String>, String> multiKeyMap = new HashMap<>();

    // [key1, key2] -> value1
    PageCacheKey k12 = new PageCacheKey("key1", "key2");
    multiKeyMap.put(k12, "value1");

    // [key3, key4] -> value2
    PageCacheKey k34 = new PageCacheKey("key3", "key4");
    multiKeyMap.put(k34, "value2");

    // print multikey map
    System.out.println(multiKeyMap);

    // print value corresponding to key1 and key2
    System.out.println(multiKeyMap.get(k12));
  }

}
