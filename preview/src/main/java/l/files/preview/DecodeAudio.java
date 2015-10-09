package l.files.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import l.files.fs.File;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeByteArray;

final class DecodeAudio extends DecodeMedia {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean accept(String mediaType) {
            return mediaType.startsWith("audio/");
        }

        @Override
        public Decode create(
                File res,
                Stat stat,
                Rect constraint,
                PreviewCallback callback,
                Preview context) {
            return new DecodeAudio(res, stat, constraint, callback, context);
        }

    };

    DecodeAudio(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(res, stat, constraint, callback, context);
    }

    @Override
    Bitmap decode(MediaMetadataRetriever retriever) {
        byte[] data = retriever.getEmbeddedPicture();
        if (data == null) {
            return null;
        }
        return decodeByteArray(data, 0, data.length);
    }

}
