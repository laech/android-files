package l.files.ui.preview;

import android.graphics.Bitmap;

import java.io.IOException;
import java.io.InputStream;

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Paths;
import l.files.fs.Stat;
import l.files.testing.fs.PathBaseTest;
import l.files.ui.base.graphics.Rect;

import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MINUTES;
import static l.files.fs.Files.createFile;
import static l.files.fs.Files.createSymbolicLink;
import static l.files.fs.Files.writeUtf8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public final class PreviewTest extends PathBaseTest {

    private Preview newPreview() {
        return new Preview(getContext(), dir2().concat(String.valueOf(nanoTime())));
    }

    public void test_preview_svg() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.svg");
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

    public void test_preview_correctly_without_wrong_file_extension() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.jpg", "a.pdf");
        testPreviewSuccessForTestFile("preview_test.jpg", "b.txt");
        testPreviewSuccessForTestFile("preview_test.jpg", "c");
    }

    private void testPreviewSuccessForTestFile(String testFile) throws Throwable {
        testPreviewSuccessForTestFile(testFile, testFile);
    }

    private void testPreviewSuccessForTestFile(String testFile, String dstFileName) throws Throwable {
        Path file = dir1().concat(dstFileName);
        InputStream in = getContext().getAssets().open(testFile);
        try {
            Files.copy(in, file);
        } finally {
            in.close();
        }
        testPreviewSuccess(file);
        testPreviewSuccess(file);
    }

    public void test_preview_proc_cpuinfo() throws Throwable {
        testPreviewSuccess(Paths.get("/proc/cpuinfo"));
    }

    public void test_preview_link() throws Throwable {
        Path file = createFile(dir1().concat("file"));
        Path link = createSymbolicLink(dir1().concat("link"), file);
        writeUtf8(file, "hi");
        testPreviewSuccess(file);
        testPreviewSuccess(file);
        testPreviewSuccess(link);
        testPreviewSuccess(link);
    }

    public void test_preview_link_modified_target() throws Throwable {
        Path file = createFile(dir1().concat("file"));
        Path link = createSymbolicLink(dir1().concat("link"), file);
        testPreviewFailure(link);

        writeUtf8(file, "hi");
        testPreviewSuccess(link);
        testPreviewSuccess(link);
    }

    private void testPreviewSuccessForContent(String content) throws Throwable {
        Path file = dir1().concat(String.valueOf(nanoTime()));
        writeUtf8(file, content);
        testPreviewSuccess(file);
        testPreviewSuccess(file);
    }

    private void testPreviewSuccess(Path file) throws Throwable {
        Preview.Callback callback = mock(Preview.Callback.class);
        Stat stat = Files.stat(file, FOLLOW);
        Preview preview = newPreview();
        Rect max = Rect.of(100, 100);
        Decode task = preview.get(file, stat, max, callback);
        assertNotNull(task);

        int millis = 60000;

        verify(callback, timeout(millis)).onPreviewAvailable(
                eq(file),
                eq(stat),
                notNull(Bitmap.class));

        verify(callback, timeout(millis)).onBlurredThumbnailAvailable(
                eq(file),
                eq(stat),
                notNull(Bitmap.class));

        task.awaitAll(1, MINUTES);

        assertNotNull(preview.getSize(file, stat, max, true));
        assertNotNull(preview.getThumbnail(file, stat, max, true));
        assertNotNull(preview.getBlurredThumbnail(file, stat, max, true));
        assertNotNull(preview.getMediaType(file, stat, max, true));
        assertNotNull(preview.getThumbnailFromDisk(file, stat, max, true));
        assertNull(preview.getNoPreviewReason(file, stat, max));
    }

    private void testPreviewFailure(Path file) throws IOException {
        Preview.Callback callback = mock(Preview.Callback.class);
        Stat stat = Files.stat(file, NOFOLLOW);
        Rect rect = Rect.of(10, 10);
        assertNull(newPreview().get(file, stat, rect, callback));
    }

}
