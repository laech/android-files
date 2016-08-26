package l.files.ui.base.graphics.drawable;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.test.AndroidTestCase;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.BLUE;
import static l.files.ui.base.graphics.drawable.Drawables.toBitmap;

public final class DrawablesTest extends AndroidTestCase {

    public void test_toBitmap() throws Exception {
        Bitmap src = generateBitmap();
        Bitmap dst = toBitmap(new BitmapDrawable(getResources(), src));
        assertTrue(dst.sameAs(src));
    }

    private Bitmap generateBitmap() {
        Bitmap src = createBitmap(4, 3, ARGB_8888);
        src.eraseColor(BLUE);
        return src;
    }

    private Resources getResources() {
        return getContext().getResources();
    }

}
