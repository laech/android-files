package l.files.ui.preview

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Canvas
import android.graphics.Color.BLUE
import l.files.ui.base.graphics.Rect
import l.files.ui.base.graphics.ScaledBitmap
import org.junit.Assert.*
import org.junit.Test
import java.lang.System.currentTimeMillis
import java.nio.file.Files.*
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.attribute.FileTime
import java.util.concurrent.TimeUnit.DAYS

internal class ThumbnailDiskCacheTest :
  CacheTest<ScaledBitmap, ThumbnailDiskCache>() {

  @Test
  fun cache_file_stored_in_cache_dir() {
    val cacheFilePath = cache.cacheFile(file, time, newConstraint(), true)
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
    val cacheFile = cache.cacheFile(file, time, constraint, true)
    assertFalse(exists(cacheFile))

    cache.put(file, time, constraint, value)
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
    cache.put(file, time, constraint, value)
    val cacheFile = cache.cacheFile(file, time, constraint, true)

    val oldTime = FileTime.fromMillis(1000)
    setLastModifiedTime(cacheFile, oldTime)
    assertEquals(oldTime, getLastModifiedTime(cacheFile, NOFOLLOW_LINKS))

    cache.get(file, time, constraint, true)
    val newTime = getLastModifiedTime(cacheFile, NOFOLLOW_LINKS)
    assertNotEquals(oldTime, newTime)
    assertTrue(oldTime < newTime)
  }

  @Test
  fun constraint_is_used_as_part_of_key() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, time, constraint, value)
    assertValueEquals(value, cache.get(file, time, constraint, true))
    assertNull(cache.get(file, time, newConstraint(), true))
    assertNull(cache.get(file, time, newConstraint(), true))
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
