package l.files.thumbnail

import android.content.Context
import l.files.fs.Path
import l.files.ui.base.graphics.Bitmaps
import l.files.ui.base.graphics.Rect

internal object ImageThumbnailer : Thumbnailer<Path> {

  override fun accepts(path: Path, type: String) =
    type.startsWith("image/")

  override fun create(input: Path, max: Rect, context: Context) =
    Bitmaps.decodeScaledDownBitmap({ input.newInputStream() }, max)
}
