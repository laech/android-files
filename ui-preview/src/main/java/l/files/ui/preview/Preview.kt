package l.files.ui.preview

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.MainThread
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import java.io.IOException
import java.nio.file.Files.isReadable
import java.nio.file.Files.isRegularFile
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.Future

internal data class BlurredThumbnail(val bitmap: Bitmap)

internal data class NoPreview(val cause: Any) {
  companion object {
    val FILE_UNREADABLE = NoPreview("file is unreadable")
    val NOT_REGULAR_FILE = NoPreview("not a regular file")
    val PATH_IN_CACHE_DIR = NoPreview("path is in cache directory")
    val IN_NO_PREVIEW_CACHE = NoPreview("file marked in no preview cache")
    val DECODE_RETURNED_NULL = NoPreview("decode returned null")
  }
}

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
    time: Instant,
    constraint: Rect,
    matchTime: Boolean
  ) = blurredThumbnailMemCache.get(path, time, constraint, matchTime)

  fun putBlurredThumbnail(
    path: Path,
    time: Instant,
    constraint: Rect,
    thumbnail: Bitmap
  ) {
    blurredThumbnailMemCache.put(path, time, constraint, thumbnail)
  }

  fun getThumbnail(
    path: Path,
    time: Instant,
    constraint: Rect,
    matchTime: Boolean
  ) = thumbnailMemCache.get(path, time, constraint, matchTime)

  fun putThumbnail(
    path: Path,
    time: Instant,
    constraint: Rect,
    thumbnail: Bitmap
  ) {
    thumbnailMemCache.put(path, time, constraint, thumbnail)
  }

  @Throws(IOException::class)
  fun getThumbnailFromDisk(
    path: Path,
    time: Instant,
    constraint: Rect
  ): ScaledBitmap? = thumbnailDiskCache.get(path, time, constraint, true)

  fun putThumbnailToDiskAsync(
    path: Path,
    time: Instant,
    constraint: Rect,
    thumbnail: ScaledBitmap
  ): Future<*> = thumbnailDiskCache.putAsync(path, time, constraint, thumbnail)

  fun getSize(
    path: Path,
    time: Instant,
    constraint: Rect,
    matchTime: Boolean
  ): Rect? = sizeCache.get(path, time, constraint, matchTime)

  fun putSize(
    path: Path,
    time: Instant,
    constraint: Rect,
    size: Rect
  ) {
    sizeCache.put(path, time, constraint, size)
  }

  fun getMediaType(
    path: Path,
    time: Instant,
    constraint: Rect
  ): String? = mediaTypeCache.get(path, time, constraint, true)

  fun putMediaType(
    path: Path,
    time: Instant,
    constraint: Rect,
    media: String
  ) {
    mediaTypeCache.put(path, time, constraint, media)
  }

  internal fun getNoPreviewReason(
    path: Path,
    time: Instant,
    constraint: Rect
  ): NoPreview? {
    if (!isRegularFile(path)) {
      return NoPreview.NOT_REGULAR_FILE
    }
    if (!isReadable(path)) {
      return NoPreview.FILE_UNREADABLE
    }
    val noPreview = noPreviewCache.get(path, time, constraint, true)
    return if (java.lang.Boolean.TRUE == noPreview) {
      NoPreview.IN_NO_PREVIEW_CACHE
    } else {
      null
    }
  }

  fun isPreviewable(
    path: Path,
    time: Instant,
    constraint: Rect
  ): Boolean {
    return getNoPreviewReason(path, time, constraint) == null
  }

  fun putPreviewable(
    path: Path,
    time: Instant,
    constraint: Rect,
    previewable: Boolean
  ) {
    if (previewable) {
      noPreviewCache.remove(path, constraint)
    } else {
      noPreviewCache.put(path, time, constraint, true)
    }
  }

  fun get(
    path: Path,
    time: Instant,
    constraint: Rect,
    callback: Callback,
    context: Context
  ): Decode? = if (!isPreviewable(path, time, constraint)) {
    null
  } else {
    Decode(path, time, constraint, callback, this)
      .executeOnPreferredExecutor(context)
  }

  fun clearThumbnailCache() {
    thumbnailMemCache.clear()
  }

  fun clearBlurredThumbnailCache() {
    blurredThumbnailMemCache.clear()
  }

  interface Callback {
    fun onPreviewAvailable(
      path: Path,
      time: Instant,
      thumbnail: Bitmap
    )

    fun onBlurredThumbnailAvailable(
      path: Path,
      time: Instant,
      thumbnail: Bitmap
    )

    fun onPreviewFailed(path: Path, time: Instant, cause: Any)
  }

}

private var instance: Preview? = null

@MainThread
fun Context.getPreview(): Preview {
  if (instance == null) {
    instance = Preview(applicationContext) {
      (externalCacheDir ?: cacheDir).toPath()
    }
    instance!!.cleanupAsync()
  }
  return instance!!
}
