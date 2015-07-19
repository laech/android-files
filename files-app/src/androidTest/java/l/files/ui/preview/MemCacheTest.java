package l.files.ui.preview;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class MemCacheTest<V, C extends MemCache<V>>
    extends CacheTest<V, C> {

  public void test_removed_item_no_longer_available() throws Exception {
    Resource res = dir1();
    Stat stat = res.stat(NOFOLLOW);
    Rect constraint = newConstraint();
    V value = newValue();
    assertNull(cache.remove(res, stat, constraint));

    cache.put(res, stat, constraint, value);
    assertEquals(value, cache.remove(res, stat, constraint).get());
    assertNull(cache.remove(res, stat, constraint).get());
    assertNull(cache.get(res, stat, constraint));
  }

}
