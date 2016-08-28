package l.files.ui.preview;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.thumbnail.PathStreamThumbnailer;
import l.files.thumbnail.SvgThumbnailer;
import l.files.thumbnail.Thumbnailer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

final class DecodeSvg extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(
                Path path,
                String extensionInLowercase) {
            return extensionInLowercase.equals("svg");
        }

        @Override
        public boolean acceptsMediaType(
                Path path,
                String mediaTypeInLowercase) {
            return mediaTypeInLowercase.startsWith("image/svg+xml");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeSvg(path, stat, constraint, callback, using, context);
        }

    };

    private final Thumbnailer<Path> thumbnailer = new PathStreamThumbnailer(new SvgThumbnailer());

    DecodeSvg(
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
        return thumbnailer.create(path, constraint, context.context);
    }

}
