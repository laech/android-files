package l.files.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.TypedValue.COMPLEX_UNIT_PT
import android.util.TypedValue.applyDimension
import l.files.fs.Path
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap

internal object PdfThumbnailer : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    type == "application/pdf"

  override fun create(input: Path, max: Rect, context: Context): ScaledBitmap? {
    val metrics = context.resources.displayMetrics
    val pathByteArray = input.toString().toByteArray()
    // The PDF lib is not thread safe, need a global lock.
    return synchronized(this) { lockedCreate(pathByteArray, max, metrics) }
  }

  private fun lockedCreate(
    path: ByteArray,
    max: Rect,
    metrics: DisplayMetrics
  ) = Pdf.open(path).let { doc ->
    try {
      val page = Pdf.openPage(doc, 0)
      try {
        val originalSize = getSize(page, metrics)
        val scaledSize = originalSize.scaleDown(max)
        val bitmap = renderPage(page, scaledSize, metrics)
        ScaledBitmap(bitmap, originalSize)
      } finally {
        Pdf.closePage(page)
      }
    } finally {
      Pdf.close(doc)
    }
  }

  private fun getSize(page: Long, metrics: DisplayMetrics) = Rect.of(
    pointToPixel(Pdf.getPageWidthInPoints(page).toFloat(), metrics),
    pointToPixel(Pdf.getPageHeightInPoints(page).toFloat(), metrics)
  )

  private fun renderPage(
    page: Long,
    size: Rect,
    metrics: DisplayMetrics
  ) = Bitmap.createBitmap(size.width(), size.height(), ARGB_8888).also {
    it.density = metrics.densityDpi
    it.eraseColor(Color.WHITE)
    Pdf.render(page, it)
  }

  private fun pointToPixel(point: Float, metrics: DisplayMetrics) =
    (applyDimension(COMPLEX_UNIT_PT, point, metrics) + 0.5f).toInt()
}
