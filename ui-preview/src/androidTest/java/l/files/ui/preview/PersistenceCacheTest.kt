package l.files.ui.preview

import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.nio.file.Path

internal abstract class PersistenceCacheTest<V, C : PersistenceCache<V>> :
  MemCacheTest<Path, V, C>() {

  @Test
  fun removed_item_will_not_be_persisted() {
    val constraint = newConstraint()
    val value: V = newValue()

    val c1: C = newCache()
    c1.put(file, stat, constraint, value)
    c1.writeIfNeeded()

    val c2: C = newCache()
    c2.readIfNeeded()
    assertValueEquals(value, c2[file, stat, constraint, true])

    c2.remove(file, constraint)
    c2.writeIfNeeded()

    val c3: C = newCache()
    c3.readIfNeeded()
    assertNull(c3[file, stat, constraint, true])
  }

  @Test
  fun reads_persisted_cache_from_put() {
    val constraint = newConstraint()
    val value: V = newValue()

    val c1: C = newCache()
    c1.put(file, stat, constraint, value)
    c1.writeIfNeeded()

    val c2: C? = newCache()
    assertNull(c2!![file, stat, constraint, true])

    c2.readIfNeeded()
    assertValueEquals(value, c2[file, stat, constraint, true])
  }

  @Test
  fun constraint_is_not_used_as_part_of_key() {
    val value: V = newValue()
    cache.put(file, stat, newConstraint(), value)
    assertValueEquals(value, cache[file, stat, newConstraint(), true])
    assertValueEquals(value, cache[file, stat, newConstraint(), true])
    assertValueEquals(value, cache[file, stat, newConstraint(), true])
    assertNotEquals(newConstraint(), newConstraint())
  }
}
