package l.files.ui.preview;

import android.graphics.Bitmap;

import l.files.common.graphics.Rect;
import l.files.fs.Instant;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.test.MoreAsserts.assertNotEqual;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ThumbnailDiskCacheTest
    extends CacheTest<Bitmap, ThumbnailDiskCache> {

  public void test_cleans_old_cache_files_not_accessed_in_30_days() throws Exception {
    Resource res = dir1();
    Stat stat = res.stat(NOFOLLOW);
    Rect constraint = newConstraint();
    Bitmap value = newValue();

    Resource cacheFile = cache.cacheFile(res, stat, constraint);
    assertFalse(cacheFile.exists(NOFOLLOW));

    cache.put(res, stat, constraint, value);
    assertTrue(cacheFile.exists(NOFOLLOW));

    cache.cleanup();
    assertTrue(cacheFile.exists(NOFOLLOW));

    cacheFile.setAccessed(NOFOLLOW, Instant.ofMillis(
        currentTimeMillis() - DAYS.toMillis(29)));
    cache.cleanup();
    assertTrue(cacheFile.exists(NOFOLLOW));

    cacheFile.setAccessed(NOFOLLOW, Instant.ofMillis(
        currentTimeMillis() - DAYS.toMillis(31)));
    cache.cleanup();
    assertFalse(cacheFile.exists(NOFOLLOW));
    assertFalse(cacheFile.parent().exists(NOFOLLOW));
  }

  public void test_updates_access_time_on_read() throws Exception {
    Resource res = dir1();
    Stat stat = res.stat(NOFOLLOW);
    Rect constraint = newConstraint();
    Bitmap value = newValue();

    cache.put(res, stat, constraint, value);

    Resource cacheFile = cache.cacheFile(res, stat, constraint);
    Instant oldTime = Instant.ofMillis(
        currentTimeMillis() - DAYS.toMillis(99));
    cacheFile.setAccessed(NOFOLLOW, oldTime);
    assertEquals(oldTime, cacheFile.stat(NOFOLLOW).atime());

    cache.get(res, stat, constraint);
    Instant newTime = cacheFile.stat(NOFOLLOW).atime();
    assertNotEqual(oldTime, newTime);
    assertTrue(oldTime.to(DAYS) < newTime.to(DAYS));
  }

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
