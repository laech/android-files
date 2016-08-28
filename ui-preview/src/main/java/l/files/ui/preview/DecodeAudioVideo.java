package l.files.ui.preview;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.thumbnail.MediaThumbnailer;
import l.files.thumbnail.Thumbnailer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

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

    private final Thumbnailer<Path> thumbnailer;

    DecodeAudioVideo(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
        thumbnailer = new MediaThumbnailer(context.context);
    }

    @Override
    ScaledBitmap decode() throws Exception {
        return thumbnailer.create(path, constraint);
    }

}
