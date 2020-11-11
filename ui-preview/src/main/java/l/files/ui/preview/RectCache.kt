package l.files.ui.preview

import l.files.ui.base.graphics.Rect
import java.io.DataInput
import java.io.DataOutput
import java.nio.file.Path

internal class RectCache(cacheDir: () -> Path) :
  PersistenceCache<Rect>(cacheDir, 1) {

  override val cacheFileName = "sizes"

  override fun read(input: DataInput): Rect {
    val width = input.readInt()
    val height = input.readInt()
    return Rect.of(width, height)
  }

  override fun write(out: DataOutput, value: Rect) {
    out.writeInt(value.width())
    out.writeInt(value.height())
  }
}
