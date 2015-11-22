package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.File;
import l.files.fs.Stat;
import l.files.fs.local.LocalFile;
import l.files.testing.fs.FileBaseTest;

import static java.lang.System.nanoTime;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public final class PreviewTest extends FileBaseTest {

    private Preview preview;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        preview = new Preview(getContext(), dir2());
    }

    @Test
    public void preview_pdf() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.pdf");
    }

    @Test
    public void preview_m4a() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.m4a");
    }

    @Test
    public void preview_jpg() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.jpg");
    }

    @Test
    public void preview_png() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.png");
    }

    @Test
    public void preview_mp4() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.mp4");
    }

    @Test
    public void preview_apk() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.apk");
    }

    @Test
    public void preview_plain_text() throws Throwable {
        testPreviewSuccessForContent("hello world");
    }

    @Test
    public void preview_xml() throws Throwable {
        testPreviewSuccessForContent("<?xml version=\"1.0\"><hello>world</hello>");
    }

    private void testPreviewSuccessForTestFile(String testFile) throws Throwable {
        File file = dir1().resolve(testFile);
        Closer closer = Closer.create();
        try {
            InputStream in = closer.register(getTestContext().getAssets().open(testFile));
            file.copyFrom(in);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        testPreviewSuccess(file);
    }

    @Test
    public void preview_proc_cpuinfo() throws Exception {
        testPreviewSuccess(LocalFile.of("/proc/cpuinfo"));
    }

    @Test
    public void preview_link() throws Exception {
        File file = dir1().resolve("file").createFile();
        File link = dir1().resolve("link").createLink(file);
        file.writeAllUtf8("hi");
        testPreviewSuccess(file);
        testPreviewSuccess(link);
    }

    @Test
    public void preview_link_modified_target() throws Exception {
        File file = dir1().resolve("file").createFile();
        File link = dir1().resolve("link").createLink(file);
        testPreviewFailure(link);

        file.writeAllUtf8("hi");
        testPreviewSuccess(link);
    }

    private void testPreviewSuccessForContent(String content) throws Throwable {
        File file = dir1().resolve(String.valueOf(nanoTime()));
        file.writeAllUtf8(content);
        testPreviewSuccess(file);
    }

    private void testPreviewSuccess(File file) throws IOException {
        PreviewCallback callback = mock(PreviewCallback.class);
        Stat stat = file.stat(FOLLOW);
        assertNotNull(preview.get(file, stat, Rect.of(10, 10), callback));

        int millis = 5000;
        verify(callback, timeout(millis)).onPreviewAvailable(eq(file), eq(stat), notNull(Bitmap.class));
        verify(callback, timeout(millis)).onSizeAvailable(eq(file), eq(stat), notNull(Rect.class));
        verify(callback, timeout(millis)).onPaletteAvailable(eq(file), eq(stat), notNull(Palette.class));
        verify(callback, never()).onPreviewFailed(eq(file), eq(stat));
    }

    private void testPreviewFailure(File file) throws IOException {
        PreviewCallback callback = mock(PreviewCallback.class);
        assertNull(preview.get(file, file.stat(NOFOLLOW), Rect.of(10, 10), callback));
    }

}
