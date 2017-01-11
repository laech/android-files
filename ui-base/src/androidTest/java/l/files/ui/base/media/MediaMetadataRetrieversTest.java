package l.files.ui.base.media;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import l.files.base.Consumer;
import l.files.fs.FileSystem;
import l.files.fs.Path;
import l.files.fs.local.LocalFileSystem;
import l.files.testing.fs.Files;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static java.io.File.createTempFile;
import static l.files.testing.fs.Files.deleteIfExists;
import static l.files.ui.base.media.MediaMetadataRetrievers.getEmbeddedThumbnail;
import static l.files.ui.base.media.MediaMetadataRetrievers.getFrameAtAnyTimeThumbnail;

public final class MediaMetadataRetrieversTest extends AndroidTestCase {

    private final FileSystem fs = LocalFileSystem.INSTANCE;

    public void test_getFrameAtAnyTimeThumbnail() throws Exception {
        String name = "MediaMetadataRetrieversTest.mp4";
        testGetThumbnail(name, new Consumer<MediaMetadataRetriever>() {
            @Override
            public void accept(MediaMetadataRetriever retriever) {
                Rect max = Rect.of(72, 1000);
                ScaledBitmap result = getFrameAtAnyTimeThumbnail(retriever, max);
                assertNotNull(result);
                assertFalse(result.bitmap().isRecycled());
                assertEquals(Rect.of(720, 1280), result.originalSize());
                assertEquals(Rect.of(72, 128), Rect.of(result.bitmap()));
            }
        });
    }

    public void test_getEmbeddedThumbnail() throws Exception {
        String name = "MediaMetadataRetrieversTest.m4a";
        testGetThumbnail(name, new Consumer<MediaMetadataRetriever>() {
            @Override
            public void accept(MediaMetadataRetriever retriever) {
                Rect max = Rect.of(10, 1000);
                ScaledBitmap result;
                try {
                    result = getEmbeddedThumbnail(retriever, max);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                assertNotNull(result);
                assertFalse(result.bitmap().isRecycled());
                assertEquals(max.width(), result.bitmap().getWidth());
            }
        });
    }

    private void testGetThumbnail(
            String testFileName,
            Consumer<MediaMetadataRetriever> test) throws Exception {

        Path path = createTestFile(testFileName);
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                Uri uri = Uri.fromFile(path.toFile());
                retriever.setDataSource(getContext(), uri);
                test.accept(retriever);
            } finally {
                retriever.release();
            }
        } finally {
            deleteIfExists(fs, path);
        }
    }

    private Path createTestFile(String name) throws IOException {
        File file = createTempFile("MediaMetadataRetrieversTest", null);
        try {
            Path path = Path.fromFile(file);
            InputStream in = getContext().getAssets().open(name);
            try {
                Files.copy(in, fs, path);
            } finally {
                in.close();
            }
            return path;
        } catch (Throwable e) {
            assertTrue(file.delete() || !file.exists());
            throw e;
        }
    }

}