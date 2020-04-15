package l.files.ui.preview

import l.files.fs.Instant.EPOCH
import l.files.fs.LinkOption
import l.files.fs.LinkOption.NOFOLLOW
import l.files.fs.Path
import l.files.fs.Stat
import l.files.ui.base.graphics.Rect
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.util.*

internal abstract class CacheTest<V, C : Cache<V>> {

  lateinit var cache: C
  lateinit var random: Random
  lateinit var file: Path
  lateinit var stat: Stat
  private lateinit var tempDir: File

  private fun createTempDir(): File {
    val dir = File.createTempFile(javaClass.simpleName, null)
    assertTrue(dir.delete())
    assertTrue(dir.mkdir())
    return dir
  }

  @Before
  fun setUp() {
    tempDir = createTempDir()
    cache = newCache()
    random = Random()
    val localFile = File.createTempFile("123", null, tempDir)
    file = Path.of(localFile)
    stat = file.stat(LinkOption.FOLLOW)
  }

  @After
  fun tearDown() {
    assertTrue(tempDir.deleteRecursively() || !tempDir.exists())
  }

  @Test
  fun gets_what_has_put_in() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, stat, constraint, value)
    assertValueEquals(value, cache.get(file, stat, constraint, true))
  }

  @Test
  fun gets_null_when_time_changes() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, stat, constraint, value)
    file.setLastModifiedTime(NOFOLLOW, EPOCH)
    assertNull(cache.get(file, file.stat(NOFOLLOW), constraint, true))
  }

  @Test
  fun gets_old_value_if_stat_not_provided() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, stat, constraint, value)
    assertValueEquals(value, cache.get(file, stat, constraint, false))
  }

  abstract fun newCache(): C

  abstract fun newValue(): V

  open fun newConstraint(): Rect = Rect.of(
    random.nextInt(100) + 1,
    random.nextInt(100) + 1
  )

  open fun assertValueEquals(a: V?, b: V?) {
    assertEquals(a, b)
  }

  fun mockCacheDir(): Path = Path.of(tempDir)
}
