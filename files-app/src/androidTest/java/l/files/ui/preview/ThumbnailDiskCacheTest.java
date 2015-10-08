package l.files.ui.preview;

import l.files.common.graphics.Rect;
import l.files.fs.File;
import l.files.fs.Instant;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.test.MoreAsserts.assertNotEqual;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ThumbnailDiskCacheTest
        extends CacheTest<Thumbnail, ThumbnailDiskCache> {

    public void test_cleans_old_cache_files_not_accessed_in_30_days() throws Exception {
        File res = dir1();
        Stat stat = res.stat(NOFOLLOW);
        Rect constraint = newConstraint();
        Thumbnail value = newValue();

        File cacheFile = cache.cacheFile(res, stat, constraint);
        assertFalse(cacheFile.exists(NOFOLLOW));

        cache.put(res, stat, constraint, value);
        assertTrue(cacheFile.exists(NOFOLLOW));

        cache.cleanup();
        assertTrue(cacheFile.exists(NOFOLLOW));

        cacheFile.setLastAccessedTime(NOFOLLOW, Instant.ofMillis(
                currentTimeMillis() - DAYS.toMillis(29)));
        cache.cleanup();
        assertTrue(cacheFile.exists(NOFOLLOW));

        cacheFile.setLastAccessedTime(NOFOLLOW, Instant.ofMillis(
                currentTimeMillis() - DAYS.toMillis(31)));
        cache.cleanup();
        assertFalse(cacheFile.exists(NOFOLLOW));
        assertFalse(cacheFile.parent().exists(NOFOLLOW));
    }

    public void test_updates_access_time_on_read() throws Exception {
        File res = dir1();
        Stat stat = res.stat(NOFOLLOW);
        Rect constraint = newConstraint();
        Thumbnail value = newValue();

        cache.put(res, stat, constraint, value);

        File cacheFile = cache.cacheFile(res, stat, constraint);
        Instant oldTime = Instant.ofMillis(
                currentTimeMillis() - DAYS.toMillis(99));
        cacheFile.setLastAccessedTime(NOFOLLOW, oldTime);
        assertEquals(oldTime, cacheFile.stat(NOFOLLOW).lastAccessedTime());

        cache.get(res, stat, constraint);
        Instant newTime = cacheFile.stat(NOFOLLOW).lastAccessedTime();
        assertNotEqual(oldTime, newTime);
        assertTrue(oldTime.to(DAYS) < newTime.to(DAYS));
    }

    public void test_constraint_is_used_as_part_of_key() throws Exception {
        File res = dir1();
        Stat stat = res.stat(NOFOLLOW);
        Rect constraint = newConstraint();
        Thumbnail value = newValue();
        cache.put(res, stat, constraint, value);
        assertValueEquals(value, cache.get(res, stat, constraint));
        assertNull(cache.get(res, stat, newConstraint()));
        assertNull(cache.get(res, stat, newConstraint()));
    }

    @Override
    void assertValueEquals(Thumbnail a, Thumbnail b) {
        assertNotNull(a);
        assertNotNull(b);
        assertEquals(a.type, b.type);
        assertEquals(a.bitmap.getWidth(), b.bitmap.getWidth());
        assertEquals(a.bitmap.getHeight(), b.bitmap.getHeight());
        for (int i = 0; i < a.bitmap.getWidth(); i++) {
            for (int j = 0; j < a.bitmap.getHeight(); j++) {
                assertEquals(a.bitmap.getPixel(i, j), b.bitmap.getPixel(i, j));
            }
        }
    }

    @Override
    ThumbnailDiskCache newCache() {
        return new ThumbnailDiskCache(dir2());
    }

    @Override
    Thumbnail newValue() {
        return new Thumbnail(
                createBitmap(
                        random.nextInt(5) + 1,
                        random.nextInt(10) + 1,
                        ARGB_8888),
                Thumbnail.Type.PICTURE);
    }
}
