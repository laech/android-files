package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.BLUE;
import static android.test.MoreAsserts.assertNotEqual;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ThumbnailDiskCacheTest
        extends CacheTest<ScaledBitmap, ThumbnailDiskCache> {

    public void test_cache_file_stored_in_cache_dir() throws Exception {
        Path cacheFilePath = cache.cacheFile(this.file, stat, newConstraint(), true);
        Path cacheDirPath = cache.cacheDir;
        assertTrue(
                "\ncacheFile: " + cacheFilePath + ",\ncacheDir:  " + cacheDirPath,
                cacheFilePath.startsWith(cacheDirPath));
    }

    public void test_cleans_old_cache_files_not_accessed_in_30_days() throws Exception {
        Rect constraint = newConstraint();
        ScaledBitmap value = newValue();

        Path cacheFile = cache.cacheFile(file, stat, constraint, true);
        assertFalse(fs.exists(cacheFile, NOFOLLOW));

        cache.put(file, stat, constraint, value);
        assertTrue(fs.exists(cacheFile, NOFOLLOW));

        cache.cleanup();
        assertTrue(fs.exists(cacheFile, NOFOLLOW));

        fs.setLastModifiedTime(cacheFile, NOFOLLOW, Instant.ofMillis(
                currentTimeMillis() - DAYS.toMillis(29)));
        cache.cleanup();
        assertTrue(fs.exists(cacheFile, NOFOLLOW));

        fs.setLastModifiedTime(cacheFile, NOFOLLOW, Instant.ofMillis(
                currentTimeMillis() - DAYS.toMillis(31)));
        cache.cleanup();
        assertFalse(fs.exists(cacheFile, NOFOLLOW));
        assertFalse(fs.exists(cacheFile.parent(), NOFOLLOW));
    }

    public void test_updates_modified_time_on_read() throws Exception {
        Rect constraint = newConstraint();
        ScaledBitmap value = newValue();

        cache.put(file, stat, constraint, value);

        Path cacheFile = cache.cacheFile(file, stat, constraint, true);
        Instant oldTime = Instant.ofMillis(1000);
        fs.setLastModifiedTime(cacheFile, NOFOLLOW, oldTime);
        assertEquals(oldTime, fs.stat(cacheFile, NOFOLLOW).lastModifiedTime());

        cache.get(file, stat, constraint, true);
        Instant newTime = fs.stat(cacheFile, NOFOLLOW).lastModifiedTime();
        assertNotEqual(oldTime, newTime);
        assertTrue(oldTime.to(DAYS) < newTime.to(DAYS));
    }

    public void test_constraint_is_used_as_part_of_key() throws Exception {
        Rect constraint = newConstraint();
        ScaledBitmap value = newValue();
        cache.put(file, stat, constraint, value);
        assertValueEquals(value, cache.get(file, stat, constraint, true));
        assertNull(cache.get(file, stat, newConstraint(), true));
        assertNull(cache.get(file, stat, newConstraint(), true));
    }

    @Override
    void assertValueEquals(ScaledBitmap a, ScaledBitmap b) {
        assertNotNull(a);
        assertNotNull(b);
    }

    @Override
    ThumbnailDiskCache newCache() {
        return new ThumbnailDiskCache(mockCacheDir());
    }

    @Override
    Rect newConstraint() {
        return Rect.of(
                random.nextInt(100) + 1000,
                random.nextInt(100) + 1000
        );
    }

    @Override
    ScaledBitmap newValue() {
        Bitmap bitmap = createBitmap(
                random.nextInt(5) + 1,
                random.nextInt(10) + 1,
                ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(BLUE);
        return new ScaledBitmap(bitmap, Rect.of(bitmap));
    }

}
