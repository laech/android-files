package l.files.ui.preview

import l.files.fs.Path
import l.files.fs.Stat
import l.files.ui.base.graphics.Rect
import java.io.IOException

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
