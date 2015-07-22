package l.files.ui.preview;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.DisplayMetrics;

import java.io.IOException;

import l.files.common.graphics.Rect;
import l.files.fs.Resource;
import l.files.fs.Stat;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.Bitmap.createBitmap;
import static android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY;
import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.os.ParcelFileDescriptor.open;
import static android.util.TypedValue.COMPLEX_UNIT_PT;
import static android.util.TypedValue.applyDimension;

final class DecodePdf extends DecodeBitmap {

  DecodePdf(
      Resource res,
      Stat stat,
      Rect constraint,
      PreviewCallback callback,
      Preview context) {
    super(res, stat, constraint, callback, context);
  }

  @Override DecodePdf executeOnPreferredExecutor() {
    return (DecodePdf) executeOnExecutor(SERIAL_EXECUTOR);
  }

  static boolean isPdf(String media, Resource res) {
    /*
     * PdfRenderer will cause native crash sometime after opening an
     * invalid PDF file, so need to make sure the file is a valid PDF.
     * https://code.google.com/p/android/issues/detail?id=91625
     *
     * PdfRenderer is not thread safe, at class level, not instance level
     * https://code.google.com/p/android/issues/detail?id=93791
     */
    return res.file().isPresent() && media.equals("application/pdf");
  }

  @Override Result decode() throws IOException {
    try (ParcelFileDescriptor fd = open(res.file().get(), MODE_READ_ONLY);
         PdfRenderer renderer = new PdfRenderer(fd)) {
      if (renderer.getPageCount() <= 0) {
        return null;
      }

      if (isCancelled()) {
        return null;
      }

      try (PdfRenderer.Page page = renderer.openPage(0)) {
        int width = pointToPixel(page.getWidth());
        int height = pointToPixel(page.getHeight());
        Rect originalSize = Rect.of(width, height);
        publishProgress(originalSize);

        if (isCancelled()) {
          return null;
        }

        Rect scaledSize = originalSize.scale(constraint);
        Bitmap bitmap = createBitmap(scaledSize.width(), scaledSize.height(), ARGB_8888);
        page.render(bitmap, null, null, RENDER_MODE_FOR_DISPLAY);
        return new Result(bitmap, originalSize);
      }
    }
  }

  private int pointToPixel(int point) {
    DisplayMetrics metrics = context.displayMetrics;
    return (int) (applyDimension(COMPLEX_UNIT_PT, point, metrics) + 0.5f);
  }

}
