package l.files.ui.preview;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.thumbnail.ImageThumbnailer;
import l.files.thumbnail.Thumbnailer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

final class DecodeImage extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(Path path, String extensionInLowercase) {
            switch (extensionInLowercase) {
                case "bmp":
                case "gif":
                case "jpg":
                case "jpeg":
                case "png":
                case "webp":
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public boolean acceptsMediaType(Path path, String mediaTypeInLowercase) {
            return mediaTypeInLowercase.startsWith("image/");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeImage(path, stat, constraint, callback, using, context);
        }

    };

    private final Thumbnailer<Path> thumbnailer = new ImageThumbnailer();

    DecodeImage(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
    }

    @Override
    ScaledBitmap decode() throws Exception {
        Rect size = context.getSize(path, stat, constraint, true);
        if (size != null) {
            publishProgress(size);
        }
        return thumbnailer.create(path, constraint, context.context);
    }

}
