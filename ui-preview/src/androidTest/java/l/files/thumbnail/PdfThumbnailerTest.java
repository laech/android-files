package l.files.thumbnail;

import android.content.res.AssetManager;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import l.files.fs.Path;
import l.files.testing.fs.Paths;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static android.support.test.InstrumentationRegistry.getContext;
import static java.io.File.createTempFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public final class PdfThumbnailerTest {

    @Test
    public void create_thumbnail_from_pdf() throws Exception {
        Path path = createTestPdf();
        try {
            Rect max = Rect.of(10, 100);
            ScaledBitmap result = newThumbnailer().create(path, max, getContext());
            assertNotNull(result);
            assertEquals(max.width(), result.bitmap().getWidth());
            assertTrue(result.originalSize().width() > max.width());
            assertTrue(result.originalSize().height() > max.height());
        } finally {
            Paths.deleteIfExists(path);
        }
    }

    private Path createTestPdf() throws IOException {
        File file = createTempFile("PdfThumbnailerTest", null);
        try {
            Path path = Path.of(file);
            try (InputStream in = openTestPdf()) {
                Paths.copy(in, path);
            }
            return path;
        } catch (Throwable e) {
            assertTrue(file.delete() || !file.exists());
            throw e;
        }

    }

    private InputStream openTestPdf() throws IOException {
        return getAssets().open("PdfThumbnailerTest.pdf");
    }

    private AssetManager getAssets() {
        return getContext().getAssets();
    }

    private PdfThumbnailer newThumbnailer() {
        return new PdfThumbnailer();
    }

}