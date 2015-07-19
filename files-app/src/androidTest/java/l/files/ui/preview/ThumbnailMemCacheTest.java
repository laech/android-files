package l.files.ui.preview;

import android.graphics.Bitmap;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ThumbnailMemCacheTest
    extends MemCacheTest<Bitmap, ThumbnailMemCache> {

  public void test_constraint_is_used_as_part_of_key() throws Exception {
    Resource res = dir1();
    Stat stat = res.stat(NOFOLLOW);
    Rect constraint = newConstraint();
    Bitmap value = newValue();
    cache.put(res, stat, constraint, value);
    assertEquals(value, cache.get(res, stat, constraint));
    assertNull(cache.get(res, stat, newConstraint()));
    assertNull(cache.get(res, stat, newConstraint()));
  }

  @Override ThumbnailMemCache newCache() {
    return new ThumbnailMemCache(getTestContext(), 0.05f);
  }

  @Override Bitmap newValue() {
    return Bitmap.createBitmap(1, 1, ARGB_8888);
  }
}
