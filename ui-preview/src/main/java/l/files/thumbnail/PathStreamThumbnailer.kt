package l.files.thumbnail

import android.content.Context
import l.files.ui.base.graphics.Rect
import java.io.InputStream
import java.nio.file.Files.newInputStream
import java.nio.file.Path

internal class PathStreamThumbnailer(
  private val thumbnailer: Thumbnailer<InputStream>
) : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    thumbnailer.accepts(path, type)

  override fun create(input: Path, max: Rect, context: Context) =
    newInputStream(input).use { thumbnailer.create(it, max, context) }

}
