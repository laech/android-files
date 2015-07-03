package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;
import android.view.View;

import com.google.common.net.MediaType;

import l.files.common.graphics.ScaledSize;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY;
import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.os.ParcelFileDescriptor.open;
import static android.util.TypedValue.COMPLEX_UNIT_PT;
import static android.util.TypedValue.applyDimension;
import static l.files.common.graphics.Bitmaps.scale;

final class DecodePdf extends DecodeBitmap
{
    DecodePdf(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key)
    {
        super(context, res, stat, view, callback, key);
    }

    static boolean isPdf(final Resource res, final MediaType media)
    {
        /*
         * PdfRenderer will cause native crash sometime after opening an
         * invalid PDF file, so need to make sure the file is a valid PDF.
         * https://code.google.com/p/android/issues/detail?id=91625
         */
        return res.file().isPresent() &&
                media.type().equalsIgnoreCase("application") &&
                media.subtype().equalsIgnoreCase("pdf");
    }

    static void run(
            final Preview context,
            final Resource res,
            final Stat stat,
            final View view,
            final PreviewCallback callback,
            final String key)
    {
        /*
         * PdfRenderer is not thread safe, at class level, not instance level
         * https://code.google.com/p/android/issues/detail?id=93791
         */
        new DecodePdf(context, res, stat, view, callback, key)
                .executeOnExecutor(SERIAL_EXECUTOR);
    }

    @Override
    protected Bitmap decode() throws Exception
    {
        try (ParcelFileDescriptor fd = open(res.file().get(), MODE_READ_ONLY);
             PdfRenderer renderer = new PdfRenderer(fd))
        {
            if (renderer.getPageCount() <= 0)
            {
                return null;
            }

            try (PdfRenderer.Page page = renderer.openPage(0))
            {
                final int height = pointToPixel(page.getHeight());
                final int width = pointToPixel(page.getWidth());
                final ScaledSize size = scale(
                        width,
                        height,
                        context.maxWidth,
                        context.maxHeight);

                publishProgress(size);

                final Bitmap bitmap = createBitmap(
                        size.scaledWidth(),
                        size.scaledHeight(),
                        ARGB_8888);

                page.render(bitmap, null, null, RENDER_MODE_FOR_DISPLAY);
                return bitmap;
            }
        }
    }

    private int pointToPixel(final int point)
    {
        final DisplayMetrics metrics = view.getResources().getDisplayMetrics();
        return (int) (applyDimension(COMPLEX_UNIT_PT, point, metrics) + 0.5f);
    }
}
