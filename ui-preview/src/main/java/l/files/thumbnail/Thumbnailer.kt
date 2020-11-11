package l.files.thumbnail

import android.content.Context
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import java.nio.file.Path
import java.util.Collections.unmodifiableList

internal interface Thumbnailer<T> {

  fun accepts(path: Path, type: String): Boolean

  @Throws(Exception::class)
  fun create(input: T, max: Rect, context: Context): ScaledBitmap?

  companion object {
    // TODO
    // Need to update NoPreview cache version to invalidate
    // cache when we add a new decoder so existing files
    // marked as not previewable will get re-evaluated.
    // Order matters, from specific to general
    val all: List<Thumbnailer<Path>> = unmodifiableList(
      listOf(
        PathStreamThumbnailer(SvgThumbnailer),
        ImageThumbnailer,
        MediaThumbnailer,
        PdfThumbnailer,
        PathStreamThumbnailer(TextThumbnailer),
        ApkThumbnailer
      )
    )
  }
}
