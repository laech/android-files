package l.files.thumbnail

import android.content.Context
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
import android.util.DisplayMetrics
import android.util.TypedValue.COMPLEX_UNIT_PT
import android.util.TypedValue.applyDimension
import l.files.fs.Path
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap

internal object PdfThumbnailer : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    type == "application/pdf"

  override fun create(input: Path, max: Rect, context: Context) =
    PdfRenderer(input.newInputFileDescriptor()).use { doc ->
      if (doc.pageCount < 1) {
        return@use null
      }

      doc.openPage(0).use { page ->
        val originalSize = getSize(page, context.resources.displayMetrics)
        val scaledSize = originalSize.scaleDown(max)
        val bitmap = renderPage(
          page, scaledSize,
          context.resources.displayMetrics
        )
        ScaledBitmap(bitmap, originalSize)
      }
    }

  private fun getSize(page: PdfRenderer.Page, metrics: DisplayMetrics) =
    Rect.of(
      pointToPixel(page.width.toFloat(), metrics),
      pointToPixel(page.height.toFloat(), metrics)
    )

  private fun renderPage(
    page: PdfRenderer.Page,
    size: Rect,
    metrics: DisplayMetrics
  ) = createBitmap(size.width(), size.height(), ARGB_8888).also {
    it.density = metrics.densityDpi
    it.eraseColor(Color.WHITE)
    page.render(it, null, null, RENDER_MODE_FOR_DISPLAY)
  }

  private fun pointToPixel(point: Float, metrics: DisplayMetrics) =
    (applyDimension(COMPLEX_UNIT_PT, point, metrics) + 0.5f).toInt()
}
