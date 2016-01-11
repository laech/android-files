package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import java.io.IOException;
import java.io.InputStream;

import l.files.base.io.Closer;
import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.Stat;
import l.files.testing.fs.PathBaseTest;

import static java.lang.System.nanoTime;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createSymbolicLink;
import static l.files.fs.Files.writeUtf8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public final class PreviewTest extends PathBaseTest {

    private Preview preview;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        preview = new Preview(getContext(), dir2());
    }

    public void test_preview_pdf() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.pdf");
    }

    public void test_preview_m4a() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.m4a");
    }

    public void test_preview_jpg() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.jpg");
    }

    public void test_preview_png() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.png");
    }

    public void test_preview_mp4() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.mp4");
    }

    public void test_preview_apk() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.apk");
    }

    public void test_preview_plain_text() throws Throwable {
        testPreviewSuccessForContent("hello world");
    }

    public void test_preview_xml() throws Throwable {
        testPreviewSuccessForContent("<?xml version=\"1.0\"><hello>world</hello>");
    }

    private void testPreviewSuccessForTestFile(String testFile) throws Throwable {
        Path file = dir1().resolve(testFile);
        Closer closer = Closer.create();
        try {
            InputStream in = closer.register(getContext().getAssets().open(testFile));
            Files.copy(in, file);
        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        testPreviewSuccess(file);
    }

    public void test_preview_proc_cpuinfo() throws Exception {
        testPreviewSuccess(Paths.get("/proc/cpuinfo"));
    }

    public void test_preview_link() throws Exception {
        Path file = createFile(dir1().resolve("file"));
        Path link = createSymbolicLink(dir1().resolve("link"), file);
        writeUtf8(file, "hi");
        testPreviewSuccess(file);
        testPreviewSuccess(link);
    }

    public void test_preview_link_modified_target() throws Exception {
        Path file = createFile(dir1().resolve("file"));
        Path link = createSymbolicLink(dir1().resolve("link"), file);
        testPreviewFailure(link);

        writeUtf8(file, "hi");
        testPreviewSuccess(link);
    }

    private void testPreviewSuccessForContent(String content) throws Throwable {
        Path file = dir1().resolve(String.valueOf(nanoTime()));
        writeUtf8(file, content);
        testPreviewSuccess(file);
    }

    private void testPreviewSuccess(Path file) throws IOException {
        PreviewCallback callback = mock(PreviewCallback.class);
        Stat stat = Files.stat(file, FOLLOW);
        assertNotNull(preview.get(file, stat, Rect.of(10, 10), callback));

        int millis = 5000;
        verify(callback, timeout(millis)).onPreviewAvailable(eq(file), eq(stat), notNull(Bitmap.class));
        verify(callback, timeout(millis)).onSizeAvailable(eq(file), eq(stat), notNull(Rect.class));
        verify(callback, timeout(millis)).onPaletteAvailable(eq(file), eq(stat), notNull(Palette.class));
        verify(callback, never()).onPreviewFailed(eq(file), eq(stat));
    }

    private void testPreviewFailure(Path file) throws IOException {
        PreviewCallback callback = mock(PreviewCallback.class);
        assertNull(preview.get(file, Files.stat(file, NOFOLLOW), Rect.of(10, 10), callback));
    }

}
