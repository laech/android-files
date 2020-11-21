package l.files.ui.base.media;

import android.media.MediaMetadataRetriever;
import l.files.base.Consumer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.*;

public final class MediaMetadataRetrieversTest {

    @Test
    public void getFrameAtAnyTimeThumbnail() throws Exception {
        String name = "MediaMetadataRetrieversTest.mp4";
        testGetThumbnail(name, retriever -> {
            Rect max = Rect.of(72, 1000);
            ScaledBitmap result = MediaMetadataRetrievers
                .getFrameAtAnyTimeThumbnail(retriever, max);
            assertNotNull(result);
            assertFalse(result.bitmap().isRecycled());
            assertEquals(Rect.of(720, 1280), result.originalSize());
            assertEquals(Rect.of(72, 128), Rect.of(result.bitmap()));
        });
    }

    @Test
    public void getEmbeddedThumbnail() throws Exception {
        String name = "MediaMetadataRetrieversTest.m4a";
        testGetThumbnail(name, retriever -> {
            Rect max = Rect.of(10, 1000);
            ScaledBitmap result;
            try {
                result = MediaMetadataRetrievers
                    .getEmbeddedThumbnail(retriever, max);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            assertNotNull(result);
            assertFalse(result.bitmap().isRecycled());
            assertEquals(max.width(), result.bitmap().getWidth());
        });
    }

    private void testGetThumbnail(
        String testFileName,
        Consumer<MediaMetadataRetriever> test
    ) throws Exception {

        Path path = createTestFile(testFileName);
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(path.toString());
                test.accept(retriever);
            } finally {
                retriever.release();
            }
        } finally {
            deleteIfExists(path);
        }
    }

    private Path createTestFile(String name) throws IOException {
        Path file = Files.createTempFile("MediaMetadataRetrieversTest", null);
        try {
            try (InputStream in = getInstrumentation().getContext()
                .getAssets()
                .open(name)) {
                Files.copy(in, file, REPLACE_EXISTING);
            }
            return file;
        } catch (Throwable e) {
            try {
                deleteIfExists(file);
            } catch (Throwable suppressed) {
                e.addSuppressed(suppressed);
            }
            throw e;
        }
    }

}
