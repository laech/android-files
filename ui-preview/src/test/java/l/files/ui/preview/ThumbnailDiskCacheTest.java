package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import l.files.fs.File;
import l.files.fs.Instant;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.BLUE;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.TimeUnit.DAYS;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class ThumbnailDiskCacheTest
        extends CacheTest<Bitmap, ThumbnailDiskCache> {

    @Test
    public void cleans_old_cache_files_not_accessed_in_30_days() throws Exception {
        Rect constraint = newConstraint();
        Bitmap value = newValue();

        File cacheFile = cache.cacheFile(file, stat, constraint, true);
        assertFalse(cacheFile.exists(NOFOLLOW));

        cache.put(file, stat, constraint, value);
        assertTrue(cacheFile.exists(NOFOLLOW));

        cache.cleanup();
        assertTrue(cacheFile.exists(NOFOLLOW));

        cacheFile.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(
                currentTimeMillis() - DAYS.toMillis(29)));
        cache.cleanup();
        assertTrue(cacheFile.exists(NOFOLLOW));

        cacheFile.setLastModifiedTime(NOFOLLOW, Instant.ofMillis(
                currentTimeMillis() - DAYS.toMillis(31)));
        cache.cleanup();
        assertFalse(cacheFile.exists(NOFOLLOW));
        assertFalse(cacheFile.parent().exists(NOFOLLOW));
    }

    @Test
    public void updates_modified_time_on_read() throws Exception {
        Rect constraint = newConstraint();
        Bitmap value = newValue();

        cache.put(file, stat, constraint, value);

        File cacheFile = cache.cacheFile(file, stat, constraint, true);
        Instant oldTime = Instant.ofMillis(1000);
        cacheFile.setLastModifiedTime(NOFOLLOW, oldTime);
        assertEquals(oldTime, cacheFile.stat(NOFOLLOW).lastModifiedTime());

        cache.get(file, stat, constraint, true);
        Instant newTime = cacheFile.stat(NOFOLLOW).lastModifiedTime();
        assertNotEquals(oldTime, newTime);
        assertTrue(oldTime.to(DAYS) < newTime.to(DAYS));
    }

    @Test
    public void constraint_is_used_as_part_of_key() throws Exception {
        Rect constraint = newConstraint();
        Bitmap value = newValue();
        cache.put(file, stat, constraint, value);
        assertValueEquals(value, cache.get(file, stat, constraint, true));
        assertNull(cache.get(file, stat, newConstraint(), true));
        assertNull(cache.get(file, stat, newConstraint(), true));
    }

    @Override
    void assertValueEquals(Bitmap a, Bitmap b) {
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
    Bitmap newValue() {
        Bitmap bitmap = createBitmap(
                random.nextInt(5) + 1,
                random.nextInt(10) + 1,
                ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(BLUE);
        return bitmap;
    }

}
