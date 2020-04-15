package l.files.ui.preview

import android.graphics.Bitmap.CompressFormat.WEBP
import android.graphics.BitmapFactory
import android.os.Process
import android.os.Process.setThreadPriority
import android.util.Log
import l.files.fs.*
import l.files.fs.LinkOption.FOLLOW
import l.files.fs.LinkOption.NOFOLLOW
import l.files.fs.exception.DirectoryNotEmpty
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.lang.ref.WeakReference
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.atomic.AtomicInteger

internal class ThumbnailDiskCache(getCacheDir: () -> Path) :
  Cache<ScaledBitmap> {

  val cacheDir: Path by lazy { getCacheDir().concat("thumbnails") }

  fun cleanupAsync() {
    executor.execute {
      setThreadPriority(BACKGROUND_THREAD_PRIORITY)
      try {
        cleanup()
      } catch (e: IOException) {
        Log.w(javaClass.simpleName, "Failed to cleanup.", e)
      }
    }
  }

  @Throws(IOException::class)
  fun cleanup() {
    if (!cacheDir.exists(FOLLOW)) {
      return
    }

    val now = currentTimeMillis()
    cacheDir.traverse(TraverseOrder.POST).use {
      it.forEach { (path, _) ->
        try {
          val stat = path.stat(NOFOLLOW)
          if (stat.isDirectory) {
            try {
              path.delete()
            } catch (ignore: DirectoryNotEmpty) {
            }
          } else {
            val lastModifiedMillis = stat.lastModifiedTime().to(MILLISECONDS)
            if (MILLISECONDS.toDays(now - lastModifiedMillis) > 30) {
              path.delete()
            }
          }
        } catch (e: IOException) {
          Log.w(javaClass.simpleName, "Failed to delete $path", e)
        }
      }
    }
  }

  private fun cacheDir(path: Path, constraint: Rect): Path =
    cacheDir.concat("${path}_${constraint.width()}_${constraint.height()}")

  @Throws(IOException::class)
  internal fun cacheFile(
    path: Path,
    stat: Stat,
    constraint: Rect,
    matchTime: Boolean
  ): Path {

    var result: Path? = null
    if (!matchTime) {
      result = cacheDir(path, constraint).listPaths()
        .use { it.findFirst().orElse(null) }
    }

    if (result == null) {
      val time = stat.lastModifiedTime()
      result = cacheDir(path, constraint)
        .concat("${time.seconds()}-${time.nanos()}")
    }

    return result!!
  }

  @Throws(IOException::class)
  override fun get(
    path: Path,
    stat: Stat,
    constraint: Rect,
    matchTime: Boolean
  ): ScaledBitmap? {

    val cache = cacheFile(path, stat, constraint, matchTime)
    return try {
      newBufferedDataInputStream(cache).use { input ->
        val version = input.readByte()
        if (version != VERSION) {
          return null // TODO return failure reason to make debugging easier
        }

        val originalWidth = input.readInt()
        val originalHeight = input.readInt()
        val originalSize = Rect.of(originalWidth, originalHeight)
        val bitmap = BitmapFactory.decodeStream(input) ?: return null
        if (bitmap.width > constraint.width() || bitmap.height > constraint.height()) {
          bitmap.recycle()
          return null
        }

        try {
          cache.setLastModifiedTime(
            NOFOLLOW,
            Instant.ofMillis(currentTimeMillis())
          )
        } catch (ignore: IOException) {
        }

        ScaledBitmap(bitmap, originalSize)
      }
    } catch (e: FileNotFoundException) {
      null
    }
  }

  private fun newBufferedDataInputStream(path: Path): DataInputStream =
    DataInputStream(path.newInputStream().buffered())

  private fun newBufferedDataOutputStream(path: Path): DataOutputStream =
    DataOutputStream(path.newOutputStream(false).buffered())

  @Throws(IOException::class)
  override fun put(
    path: Path,
    stat: Stat,
    constraint: Rect,
    value: ScaledBitmap
  ): Snapshot<ScaledBitmap>? {

    purgeOldCacheFiles(path, constraint)

    val cache = cacheFile(path, stat, constraint, true)
    val parent = cache.parent()!!
    parent.createDirectories()

    val tmp = parent.concat(cache.name().toString() + "-" + System.nanoTime())
    try {
      newBufferedDataOutputStream(tmp).use { out ->
        out.writeByte(VERSION.toInt())
        out.writeInt(value.originalSize().width())
        out.writeInt(value.originalSize().height())
        value.bitmap().compress(WEBP, 100, out)
      }
    } catch (e: Exception) {
      try {
        tmp.delete()
      } catch (sup: Exception) {
        e.addSuppressed(sup)
      }
      throw e
    }

    tmp.rename(cache)
    return null
  }

  private fun purgeOldCacheFiles(
    path: Path,
    constraint: Rect
  ) {
    try {
      cacheDir(path, constraint).listPaths().use {
        it.forEach { path ->
          try {
            path.delete()
          } catch (e: IOException) {
            Log.w(javaClass.simpleName, "Failed to purge $path", e)
          }
        }
      }
    } catch (ignored: FileNotFoundException) {
    }
  }

  fun putAsync(
    path: Path,
    stat: Stat,
    constraint: Rect,
    thumbnail: ScaledBitmap
  ): Future<*> = executor.submit(
    WriteThumbnail(
      path,
      stat,
      constraint,
      WeakReference(thumbnail)
    )
  )

  private inner class WriteThumbnail(
    private val path: Path,
    private val stat: Stat,
    private val constraint: Rect,
    private val ref: WeakReference<ScaledBitmap>
  ) : () -> Unit {

    override fun invoke() {
      setThreadPriority(BACKGROUND_THREAD_PRIORITY)
      val thumbnail = ref.get() ?: return
      try {
        put(path, stat, constraint, thumbnail)
      } catch (e: IOException) {
        Log.w(javaClass.simpleName, "Failed to put $path", e)
      }
    }
  }

  companion object {
    private val executor =
      Executors.newFixedThreadPool(
        2,
        object : ThreadFactory {
          private val count =
            AtomicInteger(1)

          override fun newThread(r: Runnable): Thread {
            val prefix = "ThumbnailDiskCache #"
            val num = count.getAndIncrement()
            return Thread(r, prefix + num)
          }
        })
    private const val BACKGROUND_THREAD_PRIORITY =
      Process.THREAD_PRIORITY_LOWEST + Process.THREAD_PRIORITY_MORE_FAVORABLE
    private const val VERSION: Byte = 5
  }

}