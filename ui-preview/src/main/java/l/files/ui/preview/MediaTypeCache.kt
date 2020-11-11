package l.files.ui.preview

import java.io.DataInput
import java.io.DataOutput
import java.nio.file.Path

internal class MediaTypeCache(cacheDir: () -> Path) :
  PersistenceCache<String>(cacheDir, 1) {

  override val cacheFileName = "media-types"

  override fun read(input: DataInput): String = input.readUTF()

  override fun write(out: DataOutput, value: String) = out.writeUTF(value)
}
