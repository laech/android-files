package l.files.ui.preview

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal abstract class MemCacheTest<K, V, C : MemCache<K, V>> :
  CacheTest<V, C>() {

  @Test
  fun removed_item_no_longer_available() {
    val constraint = newConstraint()
    val value: V = newValue()
    assertNull(cache.remove(file, constraint))

    cache.put(file, stat, constraint, value)
    assertEquals(value, cache.remove(file, constraint)!!.value)
    assertNull(cache.remove(file, constraint))
    assertNull(cache[file, stat, constraint, true])
  }
}
