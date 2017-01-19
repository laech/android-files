package l.files.thumbnail;

import android.content.res.AssetManager;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import l.files.fs.local.LocalPath;
import l.files.testing.fs.ExtendedPath;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static java.io.File.createTempFile;

public final class PdfThumbnailerTest extends AndroidTestCase {

    public void test_create_thumbnail_from_pdf() throws Exception {
        ExtendedPath path = createTestPdf();
        try {
            Rect max = Rect.of(10, 100);
            ScaledBitmap result = newThumbnailer().create(path, max, getContext());
            assertNotNull(result);
            assertEquals(max.width(), result.bitmap().getWidth());
            assertTrue(result.originalSize().width() > max.width());
            assertTrue(result.originalSize().height() > max.height());
        } finally {
            path.deleteIfExists();
        }
    }

    private ExtendedPath createTestPdf() throws IOException {
        File file = createTempFile("PdfThumbnailerTest", null);
        try {
            ExtendedPath path = ExtendedPath.wrap(LocalPath.create(file));
            InputStream in = openTestPdf();
            try {
                path.copy(in);
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