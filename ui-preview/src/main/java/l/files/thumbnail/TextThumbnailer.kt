package l.files.thumbnail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Color.WHITE
import android.graphics.Typeface.MONOSPACE
import android.util.TypedValue.*
import android.view.View.MeasureSpec.*
import android.widget.TextView
import l.files.base.io.Readers
import l.files.fs.Path
import l.files.fs.media.MediaTypes.generalize
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import java.io.InputStream
import kotlin.math.min
import kotlin.text.Charsets.UTF_8

internal object TextThumbnailer : Thumbnailer<InputStream> {

  private const val PREVIEW_LIMIT = 256
  private val TEXT_COLOR = Color.parseColor("#616161")

  override fun accepts(path: Path, type: String) =
    generalize(type).startsWith("text/")

  override fun create(
    input: InputStream,
    max: Rect,
    context: Context
  ): ScaledBitmap? {
    // TODO support more charsets
    val text = Readers.readString(input, PREVIEW_LIMIT, UTF_8) ?: return null
    val bitmap = draw(text, max, context)
    // TODO this returns bitmaps of different sizes and aspect ratio
    // when on portrait and on landscape, this causes problem since
    // the size of the bitmap is used as the originalSize and saved,
    // so when we are in portrait, the saved size is used which maybe
    // of different aspect ratio then the later loaded thumbnail causing
    // view to flicker
    return ScaledBitmap(bitmap, Rect.of(bitmap))
  }

  private fun draw(text: String, max: Rect, context: Context): Bitmap {
    val metrics = context.resources.displayMetrics
    val size = min(max.width(), max.height())
    val padding = applyDimension(COMPLEX_UNIT_DIP, 8f, metrics).toInt()
    val view = TextView(context)
    view.maxLines = 10
    view.setLineSpacing(0f, 1.1f)
    view.setBackgroundColor(WHITE)
    view.setTextColor(TEXT_COLOR)
    view.typeface = MONOSPACE
    view.setTextSize(COMPLEX_UNIT_SP, 11f)
    view.setPadding(padding, padding, padding, padding)
    view.text = if (text.length == PREVIEW_LIMIT) "$text..." else text
    view.measure(
      makeMeasureSpec(size, UNSPECIFIED),
      makeMeasureSpec(size, AT_MOST)
    )
    view.layout(0, 0, size, size)
    val bitmap = Bitmap.createBitmap(
      size,
      view.measuredHeight,
      ARGB_8888
    )
    view.draw(Canvas(bitmap))
    return bitmap
  }
}
