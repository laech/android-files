package l.files.ui.base.graphics.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import org.junit.Test;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Color.BLUE;
import static android.support.test.InstrumentationRegistry.getContext;
import static l.files.ui.base.graphics.drawable.Drawables.toBitmap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class DrawablesTest {

    @Test
    public void toBitmap_no_scaling_needed() throws Exception {
        Bitmap src = createBitmap(4, 3, BLUE);
        Rect max = Rect.of(src.getWidth(), src.getHeight());
        ScaledBitmap result = toBitmap(toDrawable(src), max);
        assertNotNull(result);
        assertTrue(result.bitmap().sameAs(src));
        assertEquals(Rect.of(src), result.originalSize());
    }

    @Test
    public void toBitmap_scale_to_fit() throws Exception {
        Bitmap src = createBitmap(99, 66, BLUE);
        Rect max = Rect.of(1000, 22);
        Bitmap expected = createBitmap(33, 22, BLUE);
        ScaledBitmap result = toBitmap(toDrawable(src), max);
        assertNotNull(result);
        assertTrue(result.bitmap().sameAs(expected));
        assertEquals(Rect.of(src), result.originalSize());
    }

    private Bitmap createBitmap(int width, int height, int color) {
        Bitmap src = Bitmap.createBitmap(width, height, ARGB_8888);
        src.eraseColor(color);
        return src;
    }

    private Drawable toDrawable(Bitmap bitmap) {
        return new BitmapDrawable(getResources(), bitmap);
    }

    private Resources getResources() {
        return getContext().getResources();
    }

}
