package l.files.ui.preview;

import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;

import java.io.IOException;
import java.io.InputStream;

import l.files.fs.File;
import l.files.testing.fs.FileBaseTest;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static java.lang.System.nanoTime;
import static l.files.fs.LinkOption.FOLLOW;
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

    public void test_preview_pdf() throws Throwable {
        if (SDK_INT >= LOLLIPOP) {
            testPreviewSuccessForTestFile("preview_test.pdf");
        }
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
        File file = dir1().resolve(testFile);
        try (InputStream in = getTestContext().getAssets().open(testFile)) {
            file.copyFrom(in);
        }
        testPreviewSuccess(file);
    }

    public void test_preview_proc_cpuinfo() throws Exception {
        testPreviewSuccess(dir1().root().resolve("/proc/cpuinfo"));
    }

    public void test_preview_link() throws Exception {
        File file = dir1().resolve("file").createFile();
        File link = dir1().resolve("link").createLink(file);
        file.writeAllUtf8("hi");
        testPreviewSuccess(link);
    }

    public void test_preview_link_modified_target() throws Exception {
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
        assertNotNull(preview.get(file, file.stat(FOLLOW), Rect.of(10, 10), callback));

        int millis = 5000;
        verify(callback, timeout(millis)).onSizeAvailable(eq(file), notNull(Rect.class));
        verify(callback, timeout(millis)).onPaletteAvailable(eq(file), notNull(Palette.class));
        verify(callback, timeout(millis)).onPreviewAvailable(eq(file), notNull(Bitmap.class));
        verify(callback, never()).onPreviewFailed(eq(file));
    }

    private void testPreviewFailure(File file) throws IOException {
        PreviewCallback callback = mock(PreviewCallback.class);
        assertNull(preview.get(file, file.stat(NOFOLLOW), Rect.of(10, 10), callback));
    }

}
