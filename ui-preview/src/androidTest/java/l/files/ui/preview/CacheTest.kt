package l.files.ui.preview

import l.files.ui.base.graphics.Rect
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.util.*

internal abstract class CacheTest<V, C : Cache<V>> {

  lateinit var cache: C
  lateinit var random: Random
  lateinit var file: Path
  lateinit var time: Instant
  private lateinit var tempDir: Path

  private fun createTempDir(): Path {
    return createTempDirectory(javaClass.simpleName)
  }

  @Before
  fun setUp() {
    tempDir = createTempDir()
    cache = newCache()
    random = Random()
    file = createTempFile(tempDir, "123", null)
    time = getLastModifiedTime(file).toInstant()
  }

  @After
  fun tearDown() {
    assertTrue(tempDir.toFile().deleteRecursively() || !exists(tempDir))
  }

  @Test
  fun gets_what_has_put_in() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, time, constraint, value)
    assertValueEquals(value, cache.get(file, time, constraint, true))
  }

  @Test
  fun gets_null_when_time_changes() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, time, constraint, value)
    setLastModifiedTime(file, FileTime.fromMillis(0))
    assertNull(
      cache.get(
        file,
        getLastModifiedTime(file).toInstant(),
        constraint,
        true
      )
    )
  }

  @Test
  fun gets_old_value_if_stat_not_provided() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, time, constraint, value)
    assertValueEquals(value, cache.get(file, time, constraint, false))
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

  fun mockCacheDir(): Path = tempDir
}
