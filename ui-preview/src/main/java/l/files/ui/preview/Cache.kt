package l.files.ui.preview

import l.files.fs.Stat
import l.files.ui.base.graphics.Rect
import java.io.IOException
import java.nio.file.Path

internal data class Snapshot<V>(
  val value: V,
  val time: Long
)

internal interface Cache<V> {

  @Throws(IOException::class)
  fun get(
    path: Path,
    stat: Stat,
    constraint: Rect,
    matchTime: Boolean
  ): V?

  @Throws(IOException::class)
  fun put(
    path: Path,
    stat: Stat,
    constraint: Rect,
    value: V
  ): Snapshot<V>?
}
