package l.files.ui.preview

import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class ThumbnailMemCacheTest :
  MemCacheTest<Any, Bitmap, ThumbnailMemCache>() {

  @Test
  fun constraint_is_used_as_part_of_key() {
    val constraint = newConstraint()
    val value = newValue()
    cache.put(file, time, constraint, value)
    assertEquals(value, cache.get(file, time, constraint, true))
    assertNull(cache.get(file, time, newConstraint(), true))
    assertNull(cache.get(file, time, newConstraint(), true))
  }

  override fun newCache() = ThumbnailMemCache(1024 * 1024, true)

  override fun newValue(): Bitmap = Bitmap.createBitmap(1, 1, ARGB_8888)
}
