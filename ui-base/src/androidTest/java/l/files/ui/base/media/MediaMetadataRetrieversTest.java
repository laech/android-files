package l.files.ui.base.media;

import android.media.MediaMetadataRetriever;
import android.net.Uri;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import l.files.base.Consumer;
import l.files.fs.Path;
import l.files.testing.fs.Paths;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static androidx.test.InstrumentationRegistry.getContext;
import static java.io.File.createTempFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
            Consumer<MediaMetadataRetriever> test) throws Exception {

        Path path = createTestFile(testFileName);
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                Uri uri = path.toUri();
                retriever.setDataSource(getContext(), uri);
                test.accept(retriever);
            } finally {
                retriever.release();
            }
        } finally {
            Paths.deleteIfExists(path);
        }
    }

    private Path createTestFile(String name) throws IOException {
        File file = createTempFile("MediaMetadataRetrieversTest", null);
        try {
            Path path = Path.of(file);
            try (InputStream in = getContext().getAssets().open(name)) {
                Paths.copy(in, path);
            }
            return path;
        } catch (Throwable e) {
            assertTrue(file.delete() || !file.exists());
            throw e;
        }
    }

}