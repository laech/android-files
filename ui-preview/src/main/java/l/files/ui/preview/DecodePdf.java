package l.files.ui.preview;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.thumbnail.PdfThumbnailer;
import l.files.thumbnail.Thumbnailer;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

final class DecodePdf extends DecodeThumbnail {

    /*
     * Single thread only as the underlying lib is not thread safe.
     */
    private static final Executor executor = newSingleThreadExecutor(new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "preview-decode-pdf");
        }
    });

    static final Previewer PREVIEWER = new Previewer() {

        @Override
        public boolean acceptsFileExtension(Path path, String extensionInLowercase) {
            return isLocalFile(path) && extensionInLowercase.equals("pdf");
        }

        @Override
        public boolean acceptsMediaType(Path path, String mediaTypeInLowercase) {
            return isLocalFile(path) && mediaTypeInLowercase.equals("application/pdf");
        }

        private boolean isLocalFile(Path path) {
            return path.fileSystem().scheme().equals("file");
        }

        @Override
        public Decode create(
                Path path,
                Stat stat,
                Rect constraint,
                Preview.Callback callback,
                Preview.Using using,
                Preview context) {
            return new DecodePdf(path, stat, constraint, callback, using, context);
        }

    };

    private final Thumbnailer<Path> thumbnailer = new PdfThumbnailer();

    DecodePdf(
            Path path,
            Stat stat,
            Rect constraint,
            Preview.Callback callback,
            Preview.Using using,
            Preview context) {
        super(path, stat, constraint, callback, using, context);
    }

    @Override
    Decode executeOnPreferredExecutor() {
        return (Decode) executeOnExecutor(executor);
    }

    @Override
    ScaledBitmap decode() throws Exception {
        return thumbnailer.create(path, constraint, context.context);
    }

}
