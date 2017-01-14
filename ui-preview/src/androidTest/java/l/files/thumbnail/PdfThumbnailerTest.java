package l.files.thumbnail;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.ExtendedFileSystem;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static java.io.File.createTempFile;

public final class PdfThumbnailerTest extends AndroidTestCase {

    private final ExtendedFileSystem fs =
            new ExtendedFileSystem(LocalFileSystem.INSTANCE);

    public void test_create_thumbnail_from_pdf() throws Exception {
        Path path = createTestPdf();
        try {
            Rect max = Rect.of(10, 100);
            ScaledBitmap result = newThumbnailer().create(path, max, getContext());
            assertNotNull(result);
            assertEquals(max.width(), result.bitmap().getWidth());
            assertTrue(result.originalSize().width() > max.width());
            assertTrue(result.originalSize().height() > max.height());
        } finally {
            fs.deleteIfExists(path);
        }
    }

    private Path createTestPdf() throws IOException {
        File file = createTempFile("PdfThumbnailerTest", null);
        try {
            Path path = Path.fromFile(file);
            InputStream in = openTestPdf();
            try {
                fs.copy(in, fs, path);
            } finally {
                in.close();
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