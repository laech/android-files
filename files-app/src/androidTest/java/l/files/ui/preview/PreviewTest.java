package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import org.apache.tika.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;

import l.files.common.graphics.Rect;
import l.files.fs.File;
import l.files.fs.local.FileBaseTest;

import static l.files.fs.LinkOption.NOFOLLOW;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public final class PreviewTest extends FileBaseTest {

    private Preview preview;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        preview = new Preview(getContext(), dir2());
    }

    public void testPreviewPdf() throws Throwable {
        testPreviewSuccess("preview_test.pdf");
    }

    public void testPreviewM4a() throws Throwable {
        testPreviewSuccess("preview_test.m4a");
    }

    public void testPreviewJpg() throws Throwable {
        testPreviewSuccess("preview_test.jpg");
    }

    public void testPreviewPng() throws Throwable {
        testPreviewSuccess("preview_test.png");
    }

    public void testPreviewMp4() throws Throwable {
        testPreviewSuccess("preview_test.mp4");
    }

    public void testPreviewApk() throws Throwable {
        testPreviewSuccess("preview_test.apk");
    }

    private void testPreviewSuccess(String testFile) throws Throwable {
        final File file = dir1().resolve(testFile);

        try (InputStream in = getTestContext().getAssets().open(testFile);
             OutputStream out = file.output()) {
            IOUtils.copy(in, out);
        }

        PreviewCallback callback = mock(PreviewCallback.class);
        assertNotNull(preview.set(file, file.stat(NOFOLLOW), Rect.of(10, 10), callback));

        int millis = 5000;
        verify(callback, timeout(millis)).onSizeAvailable(eq(file), notNull(Rect.class));
        verify(callback, timeout(millis)).onPaletteAvailable(eq(file), notNull(Palette.class));
        verify(callback, timeout(millis)).onPreviewAvailable(eq(file), notNull(Bitmap.class));
        verify(callback, never()).onPreviewFailed(eq(file));
    }

}
