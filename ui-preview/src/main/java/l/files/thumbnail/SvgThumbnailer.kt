package l.files.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color.WHITE
import com.caverock.androidsvg.SVG
import l.files.fs.Path
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import java.io.InputStream

internal object SvgThumbnailer : Thumbnailer<InputStream> {

  override fun accepts(path: Path, type: String) =
    type == "image/svg+xml"

  override fun create(
    input: InputStream,
    max: Rect,
    context: Context
  ): ScaledBitmap? {
    val svg = parseSvg(input) ?: return null
    val originalSize = getSize(svg)
    val scaledSize = originalSize.scaleDown(max)
    return ScaledBitmap(
      render(svg, scaledSize, context),
      originalSize
    )
  }

  private fun parseSvg(input: InputStream) =
    SVG.getFromInputStream(input)?.takeIf {
      it.documentWidth > 0 && it.documentHeight > 0
    }

  private fun getSize(svg: SVG) = Rect.of(
    svg.documentWidth.toInt(),
    svg.documentHeight.toInt()
  )

  private fun render(svg: SVG, size: Rect, context: Context): Bitmap {
    svg.documentWidth = size.width().toFloat()
    svg.documentHeight = size.height().toFloat()
    val bitmap = createBitmap(size, context)
    return render(svg, bitmap)
  }

  private fun render(source: SVG, destination: Bitmap): Bitmap {
    val canvas = Canvas(destination)
    source.renderToCanvas(canvas)
    return destination
  }

  private fun createBitmap(size: Rect, context: Context): Bitmap {
    val metrics = context.resources.displayMetrics
    val bitmap = Bitmap.createBitmap(
      size.width(),
      size.height(),
      ARGB_8888
    )
    bitmap.density = metrics.densityDpi
    bitmap.eraseColor(WHITE)
    return bitmap
  }
}
