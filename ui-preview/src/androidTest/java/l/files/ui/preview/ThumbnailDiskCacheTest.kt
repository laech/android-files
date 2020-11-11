package l.files.ui.preview

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color.BLUE
import l.files.fs.LinkOption.NOFOLLOW
import l.files.fs.Path
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import org.junit.Assert.*
import org.junit.Test
import java.lang.System.currentTimeMillis
import java.nio.file.Files.exists
import java.nio.file.Files.setLastModifiedTime
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit.DAYS

internal class ThumbnailDiskCacheTest :
  CacheTest<ScaledBitmap, ThumbnailDiskCache>() {

  @Test
  fun cache_file_stored_in_cache_dir() {
    val cacheFilePath = cache.cacheFile(file, stat, newConstraint(), true)
    val cacheDirPath = cache.cacheDir
    assertTrue(
      "\ncacheFile: $cacheFilePath,\ncacheDir:  $cacheDirPath",
      cacheFilePath.startsWith(cacheDirPath)
    )
  }

  @Test
  fun cleans_old_cache_files_not_accessed_in_30_days() {
    val constraint = newConstraint()
    val value = newValue()
    val cacheFile = cache.cacheFile(file, stat, constraint, true)
    assertFalse(exists(cacheFile))

    cache.put(file, stat, constraint, value)
    assertTrue(exists(cacheFile))

    cache.cleanup()
    assertTrue(exists(cacheFile))

    setLastModifiedTime(
      cacheFile,
      FileTime.fromMillis(currentTimeMillis() - DAYS.toMillis(29))
    )
    cache.cleanup()
    assertTrue(exists(cacheFile))

    setLastModifiedTime(
      cacheFile,
      FileTime.fromMillis(currentTimeMillis() - DAYS.toMillis(31))
    )
    cache.cleanup()
    assertFalse(exists(cacheFile))
    assertFalse(exists(cacheFile.parent))
  }

  @Test
  fun updates_modified_time_on_read() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, stat, constraint, value)
    val cacheFile = cache.cacheFile(file, stat, constraint, true)

    val oldTime = java.time.Instant.ofEpochMilli(1000)
    setLastModifiedTime(cacheFile, FileTime.from(oldTime))
    assertEquals(oldTime, Path.of(cacheFile).stat(NOFOLLOW).lastModifiedTime())

    cache.get(file, stat, constraint, true)
    val newTime = Path.of(cacheFile).stat(NOFOLLOW).lastModifiedTime()
    assertNotEquals(oldTime, newTime)
    assertTrue(oldTime.epochSecond < newTime.epochSecond)
  }

  @Test
  fun constraint_is_used_as_part_of_key() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, stat, constraint, value)
    assertValueEquals(value, cache.get(file, stat, constraint, true))
    assertNull(cache.get(file, stat, newConstraint(), true))
    assertNull(cache.get(file, stat, newConstraint(), true))
  }

  override fun assertValueEquals(a: ScaledBitmap?, b: ScaledBitmap?) {
    assertNotNull(a)
    assertNotNull(b)
  }

  override fun newCache() = ThumbnailDiskCache { mockCacheDir() }

  override fun newConstraint(): Rect = Rect.of(
    random.nextInt(100) + 1000,
    random.nextInt(100) + 1000
  )

  override fun newValue(): ScaledBitmap {
    val bitmap = Bitmap.createBitmap(
      random.nextInt(5) + 1,
      random.nextInt(10) + 1,
      ARGB_8888
    )
    val canvas = Canvas(bitmap)
    canvas.drawColor(BLUE)
    return ScaledBitmap(bitmap, Rect.of(bitmap))
  }
}
