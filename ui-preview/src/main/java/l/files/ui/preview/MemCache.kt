package l.files.ui.preview

import androidx.collection.LruCache
import l.files.ui.base.graphics.Rect
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes
import java.time.Instant

internal abstract class MemCache<K, V> : Cache<V> {

  private fun lastModifiedTime(attrs: BasicFileAttributes): Long =
    attrs.lastModifiedTime().toMillis()

  override operator fun get(
    path: Path,
    time: Instant,
    constraint: Rect,
    matchTime: Boolean
  ): V? {
    val key = getKey(path, constraint)
    val value = delegate[key] ?: return null
    return if (matchTime && time != value.time) {
      null
    } else {
      value.value
    }
  }

  abstract fun getKey(path: Path, constraint: Rect): K

  override fun put(
    path: Path,
    time: Instant,
    constraint: Rect,
    value: V
  ): Snapshot<V>? = delegate.put(
    getKey(path, constraint),
    Snapshot(value, time)
  )

  fun remove(path: Path, constraint: Rect): Snapshot<V>? =
    delegate.remove(getKey(path, constraint))

  abstract val delegate: LruCache<K, Snapshot<V>>
}
