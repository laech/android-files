package l.files.ui.preview

import l.files.fs.Path
import java.io.DataInput
import java.io.DataOutput

internal class MediaTypeCache(cacheDir: () -> Path) :
  PersistenceCache<String>(cacheDir, 1) {

  override val cacheFileName = "media-types"

  override fun read(input: DataInput): String = input.readUTF()

  override fun write(out: DataOutput, value: String) = out.writeUTF(value)
}