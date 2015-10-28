package l.files.ui.preview;

import android.graphics.Bitmap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = JELLY_BEAN)
public final class ThumbnailMemCacheTest
        extends MemCacheTest<Bitmap, ThumbnailMemCache> {

    @Test
    public void constraint_is_used_as_part_of_key() throws Exception {
        Rect constraint = newConstraint();
        Bitmap value = newValue();
        cache.put(res, stat, constraint, value);
        assertEquals(value, cache.get(res, stat, constraint));
        assertNull(cache.get(res, stat, newConstraint()));
        assertNull(cache.get(res, stat, newConstraint()));
    }

    @Override
    ThumbnailMemCache newCache() {
        return new ThumbnailMemCache(1024 * 1024);
    }

    @Override
    Bitmap newValue() {
        return createBitmap(1, 1, ARGB_8888);
    }

}
