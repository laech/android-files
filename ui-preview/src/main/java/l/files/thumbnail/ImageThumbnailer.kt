package l.files.thumbnail

import android.content.Context
import l.files.ui.base.graphics.Bitmaps
import l.files.ui.base.graphics.Rect
import java.nio.file.Files.newInputStream
import java.nio.file.Path

internal object ImageThumbnailer : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    type.startsWith("image/")

  override fun create(input: Path, max: Rect, context: Context) =
    Bitmaps.decodeScaledDownBitmap({ newInputStream(input) }, max)
}
