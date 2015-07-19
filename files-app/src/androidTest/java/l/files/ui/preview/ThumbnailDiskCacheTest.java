package l.files.ui.preview;

import android.graphics.Bitmap;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ThumbnailDiskCacheTest
    extends CacheTest<Bitmap, ThumbnailDiskCache> {

  public void test_constraint_is_used_as_part_of_key() throws Exception {
    Resource res = dir1();
    Stat stat = res.stat(NOFOLLOW);
    Rect constraint = newConstraint();
    Bitmap value = newValue();
    cache.put(res, stat, constraint, value);
    assertValueEquals(value, cache.get(res, stat, constraint));
    assertNull(cache.get(res, stat, newConstraint()));
    assertNull(cache.get(res, stat, newConstraint()));
  }

  @Override void assertValueEquals(Bitmap a, Bitmap b) {
    assertEquals(a.getWidth(), b.getWidth());
    assertEquals(a.getHeight(), b.getHeight());
    for (int i = 0; i < a.getWidth(); i++) {
      for (int j = 0; j < a.getHeight(); j++) {
        assertEquals(a.getPixel(i, j), b.getPixel(i, j));
      }
    }
  }

  @Override ThumbnailDiskCache newCache() {
    return new ThumbnailDiskCache(getTestContext());
  }

  @Override Bitmap newValue() {
    return createBitmap(
        random.nextInt(5) + 1,
        random.nextInt(10) + 1,
        ARGB_8888);
  }
}
