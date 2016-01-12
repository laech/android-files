package l.files.ui.preview;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.WHITE;
import static android.util.TypedValue.COMPLEX_UNIT_PT;
import static android.util.TypedValue.applyDimension;
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
    Result decode() throws IOException {

        if (isCancelled()) {
            return null;
        }

        Closer closer = Closer.create();
        try {

            final long doc = Pdf.open(path.toByteArray());
            closer.register(new Closeable() {
                @Override
                public void close() throws IOException {
                    Pdf.close(doc);
                }
            });

            final long page = Pdf.openPage(doc, 0);
            closer.register(new Closeable() {
                @Override
                public void close() throws IOException {
                    Pdf.closePage(page);
                }
            });

            if (isCancelled()) {
                return null;
            }

            double pageWidthInPoints = Pdf.getPageWidthInPoints(page);
            double pageHeightInPoints = Pdf.getPageHeightInPoints(page);
            Rect originalSize = Rect.of(
                    pointToPixel(pageWidthInPoints),
                    pointToPixel(pageHeightInPoints)
            );

            if (isCancelled()) {
                return null;
            }

            Rect scaledSize = originalSize.scale(constraint);
            Bitmap bitmap = createBitmap(
                    scaledSize.width(),
                    scaledSize.height(),
                    ARGB_8888);
            bitmap.eraseColor(WHITE);

            Pdf.render(page, bitmap);

            return new Result(bitmap, originalSize);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private int pointToPixel(double point) {
        DisplayMetrics metrics = context.context.getResources().getDisplayMetrics();
        return (int) (applyDimension(COMPLEX_UNIT_PT, (float) point, metrics) + 0.5f);
    }

}
