package l.files.ui.preview

import androidx.collection.LruCache
import l.files.fs.Path
import l.files.fs.Stat
import l.files.ui.base.graphics.Rect

internal abstract class MemCache<K, V> : Cache<V> {

  private fun lastModifiedTime(stat: Stat): Long =
    stat.lastModifiedTime().toEpochMilli()

  override operator fun get(
    path: Path,
    stat: Stat,
    constraint: Rect,
    matchTime: Boolean
  ): V? {
    val key = getKey(path, constraint)
    val value = delegate[key] ?: return null
    return if (matchTime && lastModifiedTime(stat) != value.time) {
      null
    } else {
      value.value
    }
  }

  abstract fun getKey(path: Path, constraint: Rect): K

  override fun put(
    path: Path,
    stat: Stat,
    constraint: Rect,
    value: V
  ): Snapshot<V>? = delegate.put(
    getKey(path, constraint),
    Snapshot(value, lastModifiedTime(stat))
  )

  fun remove(path: Path, constraint: Rect): Snapshot<V>? =
    delegate.remove(getKey(path, constraint))

  abstract val delegate: LruCache<K, Snapshot<V>>
}
