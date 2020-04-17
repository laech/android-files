package l.files.thumbnail

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import l.files.ui.base.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream

class TextThumbnailerTest {

  @Test
  fun create_thumbnail_from_utf8() {
    val input = ByteArrayInputStream("hello world".toByteArray())
    val result = TextThumbnailer.create(
      input,
      Rect.of(10, 999),
      getInstrumentation().context
    )!!
    val square = Rect.of(10, 10)
    assertEquals(square, Rect.of(result.bitmap()))
    assertEquals(square, result.originalSize())
  }
}
