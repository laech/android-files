package l.files.thumbnail;

import android.test.AndroidTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static com.google.common.base.Charsets.UTF_8;

public final class SvgThumbnailerTest extends AndroidTestCase {

    public void test_create_thumbnail_from_svg() throws Exception {
        InputStream in = new ByteArrayInputStream(("" +
                "<svg" +
                " fill=\"#000000\"" +
                " height=\"24\"" +
                " width=\"24\"" +
                " viewBox=\"0 0 24 24\"" +
                " xmlns=\"http://www.w3.org/2000/svg\"/>").getBytes(UTF_8));
        Rect originalSize = Rect.of(24, 24);
        Rect decodedSize = Rect.of(10, 10);
        ScaledBitmap result = thumbnailer().create(in, decodedSize, getContext());
        assertNotNull(result);
        assertEquals(decodedSize, Rect.of(result.bitmap()));
        assertEquals(originalSize, result.originalSize());
    }

    private SvgThumbnailer thumbnailer() {
        return new SvgThumbnailer();
    }

}