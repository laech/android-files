package l.files.ui.preview;

import android.graphics.Bitmap;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.testing.fs.PathBaseTest;
import l.files.testing.fs.Paths;
import l.files.ui.base.graphics.Rect;

import static android.support.test.InstrumentationRegistry.getContext;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MINUTES;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public final class PreviewTest extends PathBaseTest {

    private Preview newPreview() {
        return new Preview(getContext(), dir2().concat(String.valueOf(nanoTime())));
    }

    @Test
    public void preview_svg() throws Throwable {
        testPreviewSuccessForTestFile("preview_test.svg");
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

    @Test
    public void preview_correctly_without_wrong_file_extension() throws Throwable {
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
            Paths.copy(in, file);
        } finally {
            in.close();
        }
        testPreviewSuccess(file);
        testPreviewSuccess(file);
    }

    @Test
    public void preview_proc_cpuinfo() throws Throwable {
        testPreviewSuccess(Path.create("/proc/cpuinfo"));
    }

    @Test
    public void preview_link() throws Throwable {
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);
        Paths.writeUtf8(file, "hi");
        testPreviewSuccess(file);
        testPreviewSuccess(file);
        testPreviewSuccess(link);
        testPreviewSuccess(link);
    }


    @Test
    public void preview_link_modified_target() throws Throwable {
        Path file = dir1().concat("file").createFile();
        Path link = dir1().concat("link").createSymbolicLink(file);
        testPreviewFailure(link);

        Paths.writeUtf8(file, "hi");
        testPreviewSuccess(link);
        testPreviewSuccess(link);
    }

    private void testPreviewSuccessForContent(String content) throws Throwable {
        Path file = dir1().concat(String.valueOf(nanoTime()));
        Paths.writeUtf8(file, content);
        testPreviewSuccess(file);
        testPreviewSuccess(file);
    }

    private void testPreviewSuccess(Path file) throws Throwable {
        Preview.Callback callback = mock(Preview.Callback.class);
        Stat stat = file.stat(FOLLOW);
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
        Stat stat = file.stat(NOFOLLOW);
        Rect rect = Rect.of(10, 10);
        assertNull(newPreview().get(file, stat, rect, callback));
    }

}
