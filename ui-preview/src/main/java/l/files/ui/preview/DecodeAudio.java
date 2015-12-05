package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import l.files.fs.Path;
import l.files.fs.Stat;

import static android.graphics.BitmapFactory.decodeByteArray;

final class DecodeAudio extends DecodeMedia {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean accept(Path path, String mediaType) {
            return mediaType.startsWith("audio/");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                PreviewCallback callback,
                Preview context) {
            return new DecodeAudio(path, stat, constraint, callback, context);
        }

    };

    DecodeAudio(
            Path path,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(path, stat, constraint, callback, context);
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
