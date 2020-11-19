package l.files.ui.preview

import l.files.ui.base.graphics.Rect
import java.io.IOException
import java.nio.file.Path
import java.time.Instant

internal data class Snapshot<V>(
  val value: V,
  val time: Instant
)

internal interface Cache<V> {

  @Throws(IOException::class)
  fun get(
    path: Path,
    time: Instant,
    constraint: Rect,
    matchTime: Boolean
  ): V?

  @Throws(IOException::class)
  fun put(
    path: Path,
    time: Instant,
    constraint: Rect,
    value: V
  ): Snapshot<V>?
}
