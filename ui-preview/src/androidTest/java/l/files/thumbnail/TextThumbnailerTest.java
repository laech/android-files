package l.files.thumbnail;

import android.test.AndroidTestCase;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.fs.Files.UTF_8;

public final class TextThumbnailerTest extends AndroidTestCase {

    private TextThumbnailer thumbnailer() {
        return new TextThumbnailer();
    }

    public void test_create_thumbnail_from_utf8() throws Exception {
        InputStream in = new ByteArrayInputStream("hello world".getBytes(UTF_8));
        ScaledBitmap result = thumbnailer().create(in, Rect.of(10, 999), getContext());
        Rect square = Rect.of(10, 10);
        assertNotNull(result);
        assertEquals(square, Rect.of(result.bitmap()));
        assertEquals(square, result.originalSize());
    }
}
