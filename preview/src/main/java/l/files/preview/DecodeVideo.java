package l.files.preview;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;

import l.files.fs.File;
import l.files.fs.Stat;

final class DecodeVideo extends DecodeMedia {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean accept(File file, String mediaType) {
            return mediaType.startsWith("video/");
        }

        @Override
        public Decode create(
                File res,
                Stat stat,
                Rect constraint,
                PreviewCallback callback,
                Preview context) {
            return new DecodeVideo(res, stat, constraint, callback, context);
        }

    };

    DecodeVideo(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(res, stat, constraint, callback, context);
    }

    @Override
    Bitmap decode(MediaMetadataRetriever retriever) {
        return retriever.getFrameAtTime();
    }

}
