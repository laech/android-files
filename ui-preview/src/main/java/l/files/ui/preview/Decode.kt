package l.files.ui.preview

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Process
import android.os.Process.setThreadPriority
import android.util.Log
import l.files.fs.media.MediaTypes
import l.files.thumbnail.Thumbnailer
import l.files.ui.base.content.Contexts.isDebugBuild
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import java.io.IOException
import java.nio.file.Path
import java.time.Instant
import java.util.Locale.ENGLISH
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

class Decode internal constructor(
  private val path: Path,
  private val time: Instant,
  private val constraint: Rect,
  private val callback: Preview.Callback,
  private val preview: Preview
) : AsyncTask<Context, Any, Any>() {

  @Volatile
  private var saveThumbnailToDiskTask: Future<*>? = null

  override fun onCancelled() {
    super.onCancelled()
    saveThumbnailToDiskTask?.cancel(true)
  }

  fun awaitAll(timeout: Long, unit: TimeUnit) {
    get(timeout, unit)
    saveThumbnailToDiskTask?.get(timeout, unit)
  }

  override fun doInBackground(vararg params: Context): Any? {
    setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)
    if (isCancelled) {
      return null
    }

    try {
      if (!checkThumbnailMemCache() &&
        !checkThumbnailDiskCache() &&
        !checkNoPreviewCache() &&
        !checkIsCacheFile() &&
        !decode(params[0]) &&
        !isCancelled
      ) {
        publishProgress(NoPreview.DECODE_RETURNED_NULL)
      }
    } catch (e: Throwable) {
      publishProgress(NoPreview(e))
    }

    return null
  }

  private fun decode(context: Context): Boolean {
    if (isCancelled) {
      return false
    }

    val media = decodeMediaType(context).toLowerCase(ENGLISH)
    val thumbnailer = Thumbnailer.all.firstOrNull { it.accepts(path, media) }
    return if (thumbnailer != null) {
      decode(thumbnailer, context)
      true
    } else {
      false
    }
  }

  private fun decode(thumbnailer: Thumbnailer<Path>, context: Context) {
    val result = thumbnailer.create(path, constraint, context)
    if (result == null) {
      publishProgress(NoPreview.DECODE_RETURNED_NULL)
      return
    }
    publishProgress(result)
    saveThumbnailToDiskTask = preview.putThumbnailToDiskAsync(
      path, time, constraint, result
    )
    publishBlurredIfNeeded(result.bitmap())
  }

  private fun decodeMediaType(context: Context): String {
    var mediaType = preview.getMediaType(path, time, constraint)
    if (mediaType == null) {
      mediaType = MediaTypes.detect(context, path)
      preview.putMediaType(path, time, constraint, mediaType)
    }
    return mediaType
  }

  private fun checkIsCacheFile(): Boolean =
    if (path.startsWith(preview.cacheDir)) {
      publishProgress(NoPreview.PATH_IN_CACHE_DIR)
      true
    } else {
      false
    }

  private fun checkNoPreviewCache(): Boolean {
    val reason = preview.getNoPreviewReason(path, time, constraint)
    return if (reason != null) {
      publishProgress(reason)
      true
    } else {
      false
    }
  }

  private fun checkThumbnailMemCache(): Boolean {
    val thumbnail = preview.getThumbnail(path, time, constraint, true)
    return if (thumbnail != null) {
      publishProgress(thumbnail)
      publishBlurredIfNeeded(thumbnail)
      true
    } else {
      false
    }
  }

  private fun checkThumbnailDiskCache(): Boolean {
    var thumbnail: ScaledBitmap? = null
    try {
      thumbnail = preview.getThumbnailFromDisk(path, time, constraint)
    } catch (e: IOException) {
      Log.w(javaClass.simpleName, "Failed to get disk thumbnail for $path", e)
    }
    return if (thumbnail != null) {
      publishProgress(thumbnail)
      publishBlurredIfNeeded(thumbnail.bitmap())
      true
    } else {
      false
    }
  }

  private fun publishBlurredIfNeeded(bitmap: Bitmap) {
    if (preview.getBlurredThumbnail(path, time, constraint, true) == null) {
      publishProgress(generateBlurredThumbnail(bitmap))
    }
  }

  private fun generateBlurredThumbnail(bitmap: Bitmap): BlurredThumbnail {
    return BlurredThumbnail(StackBlur.blur(bitmap))
  }

  override fun onProgressUpdate(vararg values: Any) {
    super.onProgressUpdate(*values)
    values.forEach { handleProgressUpdateValue(it) }
  }

  private fun handleProgressUpdateValue(value: Any) = when (value) {
    is Bitmap -> handleUpdate(value)
    is NoPreview -> handleUpdate(value)
    is ScaledBitmap -> handleUpdate(value)
    is BlurredThumbnail -> handleUpdate(value)
    else -> throw IllegalStateException(value.toString())
  }

  private fun handleUpdate(value: Bitmap) {
    preview.putThumbnail(path, time, constraint, value)
    preview.putPreviewable(path, time, constraint, true)
    callback.onPreviewAvailable(path, time, value)
  }

  private fun handleUpdate(value: ScaledBitmap) {
    preview.putThumbnail(path, time, constraint, value.bitmap())
    preview.putSize(path, time, constraint, value.originalSize())
    preview.putPreviewable(path, time, constraint, true)
    callback.onPreviewAvailable(path, time, value.bitmap())
  }

  private fun handleUpdate(value: BlurredThumbnail) {
    preview.putBlurredThumbnail(path, time, constraint, value.bitmap)
    callback.onBlurredThumbnailAvailable(path, time, value.bitmap)
  }

  private fun handleUpdate(value: NoPreview) {
    preview.putPreviewable(path, time, constraint, false)
    callback.onPreviewFailed(path, time, value.cause)
  }

  fun executeOnPreferredExecutor(context: Context): Decode {
    if (isDebugBuild(context)) {
      Log.i(
        javaClass.simpleName,
        "Current queue size ${queue.size}, adding $path"
      )
    }
    return executeOnExecutor(executor, context) as Decode
  }

  companion object {
    private val queue: BlockingQueue<Runnable> = LinkedBlockingQueue()
    private val threadSeq = AtomicInteger(1)
    private val executor: Executor = ThreadPoolExecutor(
      1,
      1,
      0L, TimeUnit.MILLISECONDS,
      queue,
      ThreadFactory {
        Thread(it, "preview-task-" + threadSeq.getAndIncrement())
      }
    )
  }

}
