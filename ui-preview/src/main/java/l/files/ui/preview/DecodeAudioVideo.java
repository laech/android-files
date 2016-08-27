package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.graphics.ScaledBitmap;
import l.files.ui.base.graphics.Rect;

import static android.graphics.BitmapFactory.decodeByteArray;

final class DecodeAudioVideo extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(Path path, String extensionInLowercase) {
            switch (extensionInLowercase) {
                case "3gp":
                case "mp4":
                case "m4a":
                case "mkv":
                case "acc":
                case "mp3":
                case "ogg":
                case "wav":
                case "webm":
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean acceptsMediaType(Path path, String mediaTypeInLowercase) {
            return mediaTypeInLowercase.startsWith("audio/") ||
                    mediaTypeInLowercase.startsWith("video/");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeAudioVideo(path, stat, constraint, callback, using, context);
        }

    };

    DecodeAudioVideo(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
    }

    @Override
    ScaledBitmap decode() throws IOException {
        if (isCancelled()) {
            return null;
        }

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context.context, Uri.parse(path.toUri().toString()));

            if (isCancelled()) {
                return null;
            }

            Bitmap bitmap = decode(retriever);
            if (bitmap != null) {
                Rect size = Rect.of(bitmap.getWidth(), bitmap.getHeight());
                return new ScaledBitmap(bitmap, size);
            }

        } finally {
            retriever.release();
        }
        return null;
    }

    @Nullable
    private Bitmap decode(MediaMetadataRetriever retriever) {
        byte[] data = retriever.getEmbeddedPicture();
        if (data != null) {
            return decodeByteArray(data, 0, data.length);
        }
        return retriever.getFrameAtTime();
    }

}
