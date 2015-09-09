package l.files.ui.preview;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.OperationCanceledException;
import android.os.ParcelFileDescriptor;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import l.files.common.graphics.Rect;
import l.files.fs.File;
import l.files.fs.Stat;
import l.files.logging.Logger;

import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.graphics.Bitmap.CompressFormat.JPEG;
import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.BitmapFactory.decodeByteArray;
import static android.graphics.Color.WHITE;
import static android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY;
import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.os.ParcelFileDescriptor.open;
import static android.util.TypedValue.COMPLEX_UNIT_PT;
import static android.util.TypedValue.applyDimension;
import static java.lang.Integer.parseInt;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static l.files.R.string.authority_pdfpreview;

final class DecodePdf extends DecodeBitmap {

  /*
   * Some notes:
   *
   * PdfRenderer will cause native crash sometime after opening an
   * invalid/corrupted PDF file, so we use run a ContentProvider -
   * PdfPreviewProvider on a separate sub-process (see AndroidManifest.xml)
   * for decoding, so that if it crashes, the main process won't be affected,
   * and the sub-process will be restarted by the system, and we would have
   * marked the bad file as not previewable.
   * https://code.google.com/p/android/issues/detail?id=91625
   *
   * PdfRenderer is not thread safe, at class level, not instance level,
   * therefore we use a single thread for decoding.
   * https://code.google.com/p/android/issues/detail?id=93791
   *
   * Need to use white background for decoded bitmap to avoid transparency
   * behind the PDF text, since the resulting bitmap is not transparent, use
   * JPEG compression to send the data back, using original bitmap pixel would
   * be too big. Compressing to JPEG is also much faster than WEBP (~10x).
   */

    private static final Executor requestPool = newFixedThreadPool(3);

    private final CancellationSignal signal = new CancellationSignal();

    DecodePdf(
            File res,
            Stat stat,
            Rect constraint,
            PreviewCallback callback,
            Preview context) {
        super(res, stat, constraint, callback, context);
    }

    @Override
    public void cancelAll() {
        super.cancelAll();
        signal.cancel();
    }

    @Override
    DecodePdf executeOnPreferredExecutor() {
        return (DecodePdf) executeOnExecutor(requestPool);
    }

    static boolean isPdf(String media) {
        return media.equals("application/pdf");
    }

    @Override
    Result decode() throws IOException {
        return PdfPreviewProvider.query(
                context.context, signal, res, constraint);
    }

    public static final class PdfPreviewProvider extends ContentProvider {

        private static final Logger log = Logger.get(PdfPreviewProvider.class);

        private static final String PARAM_FILE = "file";
        private static final String PARAM_MAX_WIDTH = "maxWidth";
        private static final String PARAM_MAX_HEIGHT = "maxHeight";

        private static final String COLUMN_BITMAP_BYTES = "bitmapBytes";
        private static final String COLUMN_ORIGINAL_WIDTH = "originalWidth";
        private static final String COLUMN_ORIGINAL_HEIGHT = "originalHeight";

        private static final ExecutorService decoder = newSingleThreadExecutor();

        @Nullable
        public static DecodeBitmap.Result query(
                Context context,
                CancellationSignal signal,
                File res,
                Rect constraint) {

            requireNonNull(context);
            requireNonNull(signal);
            requireNonNull(res);
            requireNonNull(constraint);

            if (!"file".equals(res.scheme())) {
                return null;
            }

            Uri uri = new Uri.Builder()
                    .scheme(SCHEME_CONTENT)
                    .authority(context.getString(authority_pdfpreview))
                    .appendQueryParameter(PARAM_FILE, res.path())
                    .appendQueryParameter(PARAM_MAX_WIDTH, String.valueOf(constraint.width()))
                    .appendQueryParameter(PARAM_MAX_HEIGHT, String.valueOf(constraint.height()))
                    .build();

            ContentResolver resolver = context.getContentResolver();
            try (Cursor cursor = resolver.query(uri, null, null, null, null, signal)) {
                if (cursor == null || !cursor.moveToFirst()) {
                    return null;
                }

                int originalWidth = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORIGINAL_WIDTH));
                int originalHeight = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ORIGINAL_HEIGHT));
                byte[] bytes = cursor.getBlob(cursor.getColumnIndexOrThrow(COLUMN_BITMAP_BYTES));
                Bitmap bitmap = bitmapFromBytes(bytes);
                return new DecodeBitmap.Result(bitmap, Rect.of(originalWidth, originalHeight));

            } catch (OperationCanceledException e) {
                log.debug(e.getMessage());
                return null;
            }
        }

        private static Bitmap bitmapFromBytes(byte[] bytes) {
            return decodeByteArray(bytes, 0, bytes.length);
        }

        private static byte[] bitmapToBytes(Bitmap bitmap) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bitmap.compress(JPEG, 100, out);
            return out.toByteArray();
        }

        @Override
        public Cursor query(
                Uri uri,
                String[] projection,
                String selection,
                String[] selectionArgs,
                String sortOrder,
                CancellationSignal signal) {

            if (signal == null) {
                log.verbose(
                        "CancellationSignal is null, is request being restarted after crash? %s",
                        uri);

            } else if (signal.isCanceled()) {
                return null;
            }

            java.io.File file = new java.io.File(uri.getQueryParameter(PARAM_FILE));
            Rect constraint = Rect.of(
                    parseInt(uri.getQueryParameter(PARAM_MAX_WIDTH)),
                    parseInt(uri.getQueryParameter(PARAM_MAX_HEIGHT)));

            return decode(file, constraint, signal);
        }

        private Cursor decode(java.io.File file, Rect constraint, CancellationSignal signal) {

            Future<Result> future = decoder.submit(
                    new ReadPreview(getContext(), file, constraint, signal));

            try {

                Result result = future.get();
                if (result != null) {

                    MatrixCursor cursor = new MatrixCursor(
                            new String[]{
                                    COLUMN_BITMAP_BYTES,
                                    COLUMN_ORIGINAL_WIDTH,
                                    COLUMN_ORIGINAL_HEIGHT
                            },
                            1
                    );

                    cursor
                            .newRow()
                            .add(COLUMN_BITMAP_BYTES, bitmapToBytes(result.maybeScaled))
                            .add(COLUMN_ORIGINAL_WIDTH, result.originalSize.width())
                            .add(COLUMN_ORIGINAL_HEIGHT, result.originalSize.height());

                    return cursor;
                }

            } catch (InterruptedException | ExecutionException e) {
                log.debug(e);
            }

            return null;
        }

        @Override
        public boolean onCreate() {
            return true;
        }

        @Override
        public Cursor query(
                Uri uri,
                String[] projection,
                String selection,
                String[] selectionArgs,
                String sortOrder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getType(Uri uri) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Uri insert(Uri uri, ContentValues values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int delete(Uri uri, String selection, String[] selectionArgs) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int update(
                Uri uri,
                ContentValues values,
                String selection,
                String[] selectionArgs) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ReadPreview implements Callable<Result> {

        private final Context context;
        private final java.io.File file;
        private final Rect constraint;
        private final CancellationSignal signal;

        private ReadPreview(
                Context context,
                java.io.File file,
                Rect constraint,
                CancellationSignal signal) {

            this.context = context.getApplicationContext();
            this.file = file;
            this.constraint = constraint;
            this.signal = signal;
        }

        @Override
        public Result call() throws Exception {

            if (isCancelled(signal)) {
                return null;
            }

            try (ParcelFileDescriptor fd = open(file, MODE_READ_ONLY);
                 PdfRenderer renderer = new PdfRenderer(fd)) {

                if (renderer.getPageCount() <= 0) {
                    return null;
                }

                if (isCancelled(signal)) {
                    return null;
                }

                try (PdfRenderer.Page page = renderer.openPage(0)) {
                    Rect originalSize = Rect.of(
                            pointToPixel(page.getWidth()),
                            pointToPixel(page.getHeight())
                    );

                    if (isCancelled(signal)) {
                        return null;
                    }

                    Rect scaledSize = originalSize.scale(constraint);
                    Bitmap bitmap = createBitmap(
                            scaledSize.width(),
                            scaledSize.height(),
                            ARGB_8888);
                    bitmap.eraseColor(WHITE);
                    page.render(bitmap, null, null, RENDER_MODE_FOR_DISPLAY);
                    return new Result(bitmap, originalSize);
                }
            }
        }

        private int pointToPixel(int point) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return (int) (applyDimension(COMPLEX_UNIT_PT, point, metrics) + 0.5f);
        }

        private boolean isCancelled(CancellationSignal signal) {
            return signal != null && signal.isCanceled();
        }

    }

}
