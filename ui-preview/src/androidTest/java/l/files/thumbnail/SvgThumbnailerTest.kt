package l.files.thumbnail;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static androidx.test.InstrumentationRegistry.getContext;
import static l.files.base.io.Charsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public final class SvgThumbnailerTest {

    @Test
    public void create_thumbnail_from_svg() throws Exception {
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