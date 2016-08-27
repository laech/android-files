package l.files.thumbnail;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;
import android.util.DisplayMetrics;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static java.io.File.createTempFile;

public final class PdfThumbnailerTest extends AndroidTestCase {

    public void test_create_thumbnail_from_pdf() throws Exception {
        Closer closer = Closer.create();
        try {

            Path path = createTestPdf(closer);
            Rect max = Rect.of(10, 100);
            ScaledBitmap result = newThumbnailer().create(path, max);
            assertNotNull(result);
            assertEquals(max.width(), result.bitmap().getWidth());
            assertTrue(result.originalSize().width() > max.width());
            assertTrue(result.originalSize().height() > max.height());

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private Path createTestPdf(Closer closer) throws IOException {
        final File file = createTempFile("PdfThumbnailerTest", null);
        final Path path = Paths.get(file);
        closer.register(new Closeable() {
            @Override
            public void close() throws IOException {
                Files.deleteIfExists(path);
            }
        });
        InputStream in = closer.register(openTestPdf());
        Files.copy(in, path);
        return path;
    }

    private InputStream openTestPdf() throws IOException {
        return getAssets().open("PdfThumbnailerTest.pdf");
    }

    private AssetManager getAssets() {
        return getContext().getAssets();
    }

    private PdfThumbnailer newThumbnailer() {
        return new PdfThumbnailer(getDisplayMetrics());
    }

    private DisplayMetrics getDisplayMetrics() {
        return getContext().getResources().getDisplayMetrics();
    }
}