package l.files.ui.preview

import l.files.fs.Path
import java.io.DataInput
import java.io.DataOutput

internal class NoPreviewCache(cacheDir: () -> Path) :
  PersistenceCache<Boolean>(cacheDir, 2) {

  override val cacheFileName = "non-images"

  override fun read(input: DataInput): Boolean = input.readBoolean()

  override fun write(out: DataOutput, value: Boolean) = out.writeBoolean(value)
}
