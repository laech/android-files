package l.files.ui.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import l.files.fs.Path;
import l.files.fs.Stat;

final class DecodeVideo extends DecodeMedia {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean accept(Path path, String mediaType) {
            return mediaType.startsWith("video/");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                PreviewCallback callback,
                Preview context) {
            return new DecodeVideo(path, stat, constraint, callback, context);
        }

    };

    DecodeVideo(
            Path path,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(path, stat, constraint, callback, context);
    }

    @Override
    Bitmap decode(MediaMetadataRetriever retriever) {
        return retriever.getFrameAtTime();
    }

}
