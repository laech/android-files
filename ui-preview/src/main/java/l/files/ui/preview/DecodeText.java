package l.files.ui.preview;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.media.MediaTypes;
import l.files.thumbnail.PathStreamThumbnailer;
import l.files.thumbnail.TextThumbnailer;
import l.files.thumbnail.Thumbnailer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static l.files.fs.media.MediaTypes.detectByFileExtension;

final class DecodeText extends DecodeThumbnail {

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(Path path, String extensionInLowercase) {
            return acceptsMediaType(path, detectByFileExtension(extensionInLowercase));
        }

        @Override
        public boolean acceptsMediaType(Path path, String mediaTypeInLowercase) {
            return MediaTypes.generalize(mediaTypeInLowercase).startsWith("text/");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodeText(path, stat, constraint, callback, using, context);
        }

    };

    private final Thumbnailer<Path> thumbnailer;

    DecodeText(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
        thumbnailer = new PathStreamThumbnailer(new TextThumbnailer(context.context));
    }

    @Override
    ScaledBitmap decode() throws Exception {
        return thumbnailer.create(path, constraint);
    }

}
