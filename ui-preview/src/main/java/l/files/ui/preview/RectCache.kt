package l.files.ui.preview

import l.files.fs.Path
import l.files.ui.base.graphics.Rect
import java.io.DataInput
import java.io.DataOutput

internal class RectCache(cacheDir: () -> Path) :
  PersistenceCache<Rect>(cacheDir, 1) {

  override fun cacheFileName() = "sizes"

  override fun read(input: DataInput): Rect {
    val width = input.readInt()
    val height = input.readInt()
    return Rect.of(width, height)
  }

  override fun write(out: DataOutput, rect: Rect) {
    out.writeInt(rect.width())
    out.writeInt(rect.height())
  }
}
