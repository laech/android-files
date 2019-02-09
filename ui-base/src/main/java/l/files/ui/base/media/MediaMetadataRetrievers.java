package l.files.ui.base.media;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import java.io.ByteArrayInputStream;

import androidx.annotation.Nullable;

import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.ui.base.graphics.Bitmaps.decodeScaledDownBitmap;
import static l.files.ui.base.graphics.Bitmaps.scaleDownBitmap;

public final class MediaMetadataRetrievers {

    private MediaMetadataRetrievers() {
    }

    /**
     * Gets the any available thumbnail scaled to fit within
     * {@code max} maintaining original aspect ratio.
     */
    @Nullable
    public static ScaledBitmap getAnyThumbnail(
            MediaMetadataRetriever retriever,
            Rect max) throws Exception {
        ScaledBitmap result = getEmbeddedThumbnail(retriever, max);
        if (result == null) {
            result = getFrameAtAnyTimeThumbnail(retriever, max);
        }
        return result;
    }

    /**
     * Gets a thumbnail using {@link MediaMetadataRetriever#getFrameAtTime()}
     * scaled to fit within {@code max} maintaining original aspect ratio.
     */
    @Nullable
    static ScaledBitmap getFrameAtAnyTimeThumbnail(
            MediaMetadataRetriever retriever,
            Rect max) {

        Bitmap frame = retriever.getFrameAtTime();
        if (frame == null) {
            return null;
        }
        ScaledBitmap result = scaleDownBitmap(frame, max);
        if (frame != result.bitmap()) {
            frame.recycle();
        }
        return result;
    }


    /**
     * Gets a thumbnail using {@link MediaMetadataRetriever#getEmbeddedPicture()}
     * scaled to fit within {@code max} maintaining original aspect ratio.
     */
    @Nullable
    static ScaledBitmap getEmbeddedThumbnail(
            MediaMetadataRetriever retriever,
            Rect max) throws Exception {

        byte[] data = retriever.getEmbeddedPicture();
        if (data == null) {
            return null;
        }
        return decodeScaledDownBitmap(() -> new ByteArrayInputStream(data), max);
    }

}
