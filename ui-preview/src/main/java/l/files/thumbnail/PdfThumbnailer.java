package l.files.thumbnail;

import android.graphics.Bitmap;
import android.util.DisplayMetrics;

import java.io.Closeable;
import java.io.IOException;

import l.files.base.io.Closer;
import l.files.fs.Path;
import l.files.ui.base.graphics.Rect;
import l.files.ui.base.graphics.ScaledBitmap;
import l.files.ui.preview.Pdf;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.WHITE;
import static android.util.TypedValue.COMPLEX_UNIT_PT;
import static android.util.TypedValue.applyDimension;
import static l.files.base.Objects.requireNonNull;

public final class PdfThumbnailer implements Thumbnailer<Path> {

    private final DisplayMetrics metrics;

    public PdfThumbnailer(DisplayMetrics metrics) {
        this.metrics = requireNonNull(metrics, "metrics");
    }

    @Override
    public ScaledBitmap create(Path path, Rect max) throws IOException {

        Closer closer = Closer.create();
        try {

            long doc = openDoc(path, closer);
            long page = openPage(doc, closer);
            Rect originalSize = getSize(page, metrics);
            Rect scaledSize = originalSize.scaleDown(max);
            Bitmap bitmap = renderPage(page, scaledSize, metrics);
            return new ScaledBitmap(bitmap, originalSize);

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
    }

    private static long openDoc(Path path, Closer closer) throws IOException {
        final long doc = Pdf.open(path.toByteArray());
        closer.register(new Closeable() {
            @Override
            public void close() throws IOException {
                Pdf.close(doc);
            }
        });
        return doc;
    }

    private static long openPage(long doc, Closer closer) throws IOException {
        final long page = Pdf.openPage(doc, 0);
        closer.register(new Closeable() {
            @Override
            public void close() throws IOException {
                Pdf.closePage(page);
            }
        });
        return page;
    }

    private static Rect getSize(long page, DisplayMetrics metrics) throws IOException {
        double pageWidthInPoints = Pdf.getPageWidthInPoints(page);
        double pageHeightInPoints = Pdf.getPageHeightInPoints(page);
        return Rect.of(
                pointToPixel((float) pageWidthInPoints, metrics),
                pointToPixel((float) pageHeightInPoints, metrics));
    }

    private static Bitmap renderPage(
            long page, Rect size, DisplayMetrics metrics) throws IOException {

        Bitmap bitmap = createBitmap(size.width(), size.height(), ARGB_8888);
        bitmap.setDensity(metrics.densityDpi);
        bitmap.eraseColor(WHITE);
        Pdf.render(page, bitmap);
        return bitmap;
    }

    private static int pointToPixel(float point, DisplayMetrics metrics) {
        float dimen = applyDimension(COMPLEX_UNIT_PT, point, metrics);
        return (int) (dimen + 0.5f);
    }

}
