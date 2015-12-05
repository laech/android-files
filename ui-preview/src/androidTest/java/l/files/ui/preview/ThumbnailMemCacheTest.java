package l.files.ui.preview;

import android.graphics.Bitmap;

import org.junit.Test;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public final class ThumbnailMemCacheTest
        extends MemCacheTest<Bitmap, ThumbnailMemCache> {

    @Test
    public void constraint_is_used_as_part_of_key() throws Exception {
        Rect constraint = newConstraint();
        Bitmap value = newValue();
        cache.put(file, stat, constraint, value);
        assertEquals(value, cache.get(file, stat, constraint, true));
        assertNull(cache.get(file, stat, newConstraint(), true));
        assertNull(cache.get(file, stat, newConstraint(), true));
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
