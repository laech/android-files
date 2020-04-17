package l.files.thumbnail

import android.content.Context
import l.files.fs.Path
import l.files.ui.base.graphics.Rect
import java.io.InputStream

internal class PathStreamThumbnailer(
  private val thumbnailer: Thumbnailer<InputStream>
) : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    thumbnailer.accepts(path, type)

  override fun create(input: Path, max: Rect, context: Context) =
    input.newInputStream().use { thumbnailer.create(it, max, context) }

}
