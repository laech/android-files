package l.files.ui.base.media;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.test.AndroidTestCase;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import l.files.base.Consumer;
import l.files.fs.Path;
import l.files.fs.local.LocalPath;
import l.files.testing.fs.ExtendedPath;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static java.io.File.createTempFile;
import static l.files.ui.base.media.MediaMetadataRetrievers.getEmbeddedThumbnail;
import static l.files.ui.base.media.MediaMetadataRetrievers.getFrameAtAnyTimeThumbnail;

public final class MediaMetadataRetrieversTest extends AndroidTestCase {

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
                Uri uri = path.toUri();
                retriever.setDataSource(getContext(), uri);
                test.accept(retriever);
            } finally {
                retriever.release();
            }
        } finally {
            ExtendedPath.wrap(path).deleteIfExists();
        }
    }

    private Path createTestFile(String name) throws IOException {
        File file = createTempFile("MediaMetadataRetrieversTest", null);
        try {
            Path path = LocalPath.fromFile(file);
            InputStream in = getContext().getAssets().open(name);
            try {
                ExtendedPath.wrap(path).copy(in);
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