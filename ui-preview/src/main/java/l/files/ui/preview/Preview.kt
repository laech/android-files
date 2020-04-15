package l.files.ui.preview

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.MainThread
import l.files.fs.Path
import l.files.fs.Stat
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import java.io.IOException
import java.util.concurrent.Future

class Preview internal constructor(
  context: Context,
  getCacheDir: () -> Path
) {

  internal val cacheDir by lazy { getCacheDir() }
  private val sizeCache = RectCache { cacheDir }
  private val mediaTypeCache = MediaTypeCache { cacheDir }
  private val noPreviewCache = NoPreviewCache { cacheDir }
  private val thumbnailMemCache = ThumbnailMemCache(context, true, 0.20f)
  private val thumbnailDiskCache = ThumbnailDiskCache { cacheDir }

  // TODO refactor so that size here and size in decoders are in same place
  private val blurredThumbnailMemCache =
    ThumbnailMemCache(context, false, 0.05f)

  fun writeCacheAsyncIfNeeded() {
    sizeCache.writeAsyncIfNeeded()
    mediaTypeCache.writeAsyncIfNeeded()
    noPreviewCache.writeAsyncIfNeeded()
  }

  fun readCacheAsyncIfNeeded() {
    sizeCache.readAsyncIfNeeded()
    mediaTypeCache.readAsyncIfNeeded()
    noPreviewCache.readAsyncIfNeeded()
  }

  internal fun cleanupAsync() {
    thumbnailDiskCache.cleanupAsync()
  }

  fun getBlurredThumbnail(
    path: Path,
    stat: Stat,
    constraint: Rect,
    matchTime: Boolean
  ) = blurredThumbnailMemCache.get(path, stat, constraint, matchTime)

  fun putBlurredThumbnail(
    path: Path,
    stat: Stat,
    constraint: Rect,
    thumbnail: Bitmap
  ) {
    blurredThumbnailMemCache.put(path, stat, constraint, thumbnail)
  }

  fun getThumbnail(
    path: Path,
    stat: Stat,
    constraint: Rect,
    matchTime: Boolean
  ) = thumbnailMemCache.get(path, stat, constraint, matchTime)

  fun putThumbnail(
    path: Path,
    stat: Stat,
    constraint: Rect,
    thumbnail: Bitmap
  ) {
    thumbnailMemCache.put(path, stat, constraint, thumbnail)
  }

  @Throws(IOException::class)
  fun getThumbnailFromDisk(
    path: Path,
    stat: Stat,
    constraint: Rect
  ): ScaledBitmap? = thumbnailDiskCache.get(path, stat, constraint, true)

  fun putThumbnailToDiskAsync(
    path: Path,
    stat: Stat,
    constraint: Rect,
    thumbnail: ScaledBitmap
  ): Future<*> = thumbnailDiskCache.putAsync(path, stat, constraint, thumbnail)

  fun getSize(
    path: Path,
    stat: Stat,
    constraint: Rect,
    matchTime: Boolean
  ): Rect? = sizeCache.get(path, stat, constraint, matchTime)

  fun putSize(
    path: Path,
    stat: Stat,
    constraint: Rect,
    size: Rect
  ) {
    sizeCache.put(path, stat, constraint, size)
  }

  fun getMediaType(
    path: Path,
    stat: Stat,
    constraint: Rect
  ): String? = mediaTypeCache.get(path, stat, constraint, true)

  fun putMediaType(
    path: Path,
    stat: Stat,
    constraint: Rect,
    media: String
  ) {
    mediaTypeCache.put(path, stat, constraint, media)
  }

  internal fun getNoPreviewReason(
    path: Path,
    stat: Stat,
    constraint: Rect
  ): NoPreview? {
    if (!stat.isRegularFile) {
      return NoPreview.NOT_REGULAR_FILE
    }
    try {
      if (!path.isReadable) {
        return NoPreview.FILE_UNREADABLE
      }
    } catch (e: IOException) {
      return NoPreview(e)
    }
    return if (java.lang.Boolean.TRUE == noPreviewCache.get(
        path,
        stat,
        constraint,
        true
      )
    ) {
      NoPreview.IN_NO_PREVIEW_CACHE
    } else {
      null
    }
  }

  fun isPreviewable(
    path: Path,
    stat: Stat,
    constraint: Rect
  ): Boolean {
    return getNoPreviewReason(path, stat, constraint) == null
  }

  fun putPreviewable(
    path: Path,
    stat: Stat,
    constraint: Rect,
    previewable: Boolean
  ) {
    if (previewable) {
      noPreviewCache.remove(path, constraint)
    } else {
      noPreviewCache.put(path, stat, constraint, true)
    }
  }

  fun get(
    path: Path,
    stat: Stat,
    constraint: Rect,
    callback: Callback,
    context: Context
  ): Decode? = if (!isPreviewable(path, stat, constraint)) {
    null
  } else {
    Decode(path, stat, constraint, callback, this)
      .executeOnPreferredExecutor(context)
  }

  fun clearThumbnailCache() {
    thumbnailMemCache.clear()
  }

  fun clearBlurredThumbnailCache() {
    blurredThumbnailMemCache.clear()
  }

  interface Callback {
    fun onPreviewAvailable(path: Path, stat: Stat, thumbnail: Bitmap)
    fun onBlurredThumbnailAvailable(path: Path, stat: Stat, thumbnail: Bitmap)
    fun onPreviewFailed(path: Path, stat: Stat, cause: Any)
  }

}

private var instance: Preview? = null

@MainThread
fun Context.getPreview(): Preview {
  if (instance == null) {
    instance = Preview(applicationContext) {
      Path.of(externalCacheDir ?: cacheDir)
    }
    instance!!.cleanupAsync()
  }
  return instance!!
}
