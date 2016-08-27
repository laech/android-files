package l.files.ui.preview;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.thumbnail.ApkThumbnailer;
import l.files.thumbnail.Thumbnailer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

final class DecodeApk extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(Path path, String extensionInLowercase) {
            return extensionInLowercase.equals("apk");
        }

        @Override
        public boolean acceptsMediaType(Path path, String mediaTypeInLowercase) {
            return mediaTypeInLowercase.equals("application/zip") &&
                    path.name().ext().equalsIgnoreCase("apk");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeApk(path, stat, constraint, callback, using, context);
        }

    };

    private final Thumbnailer<Path> thumbnailer;

    DecodeApk(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
        thumbnailer = new ApkThumbnailer(context.context.getPackageManager());
    }

    @Override
    ScaledBitmap decode() throws Exception {
        return thumbnailer.create(path, constraint);
    }

}
