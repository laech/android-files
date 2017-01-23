package l.files.thumbnail;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.support.test.InstrumentationRegistry.getContext;
import static com.google.common.base.Charsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class TextThumbnailerTest {

    private TextThumbnailer thumbnailer() {
        return new TextThumbnailer();
    }

    @Test
    public void create_thumbnail_from_utf8() throws Exception {
        InputStream in = new ByteArrayInputStream("hello world".getBytes(UTF_8));
        ScaledBitmap result = thumbnailer().create(in, Rect.of(10, 999), getContext());
        Rect square = Rect.of(10, 10);
        assertNotNull(result);
        assertEquals(square, Rect.of(result.bitmap()));
        assertEquals(square, result.originalSize());
    }
}
