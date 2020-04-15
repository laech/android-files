package l.files.ui.preview

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache
import androidx.core.content.getSystemService
import l.files.fs.Path
import l.files.ui.base.graphics.Rect

internal class ThumbnailMemCache(
  size: Int,
  private val keyIncludeConstraint: Boolean
) : MemCache<Any, Bitmap>() {

  override val delegate = object : LruCache<Any, Snapshot<Bitmap>>(size) {
    override fun sizeOf(key: Any, value: Snapshot<Bitmap>): Int =
      value.value.allocationByteCount
  }

  constructor(
    context: Context,
    keyIncludeConstraint: Boolean,
    appMemoryPercentageToUseForCache: Float
  ) : this(
    calculateSize(context, appMemoryPercentageToUseForCache),
    keyIncludeConstraint
  )

  override fun getKey(path: Path, constraint: Rect): Any =
    if (keyIncludeConstraint) {
      Pair(path, constraint)
    } else {
      path
    }

  fun clear() {
    delegate.evictAll()
  }
}

private fun calculateSize(
  context: Context,
  appMemoryPercentageToUseForCache: Float
): Int {
  require(
    !(appMemoryPercentageToUseForCache <= 0 ||
      appMemoryPercentageToUseForCache >= 1)
  )
  val manager = context.getSystemService<ActivityManager>()!!
  val megabytes = manager.memoryClass
  val bytes = megabytes * 1024 * 1024
  return (bytes * appMemoryPercentageToUseForCache).toInt()
}
