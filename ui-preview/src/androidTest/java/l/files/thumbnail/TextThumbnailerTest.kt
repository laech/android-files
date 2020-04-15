package l.files.thumbnail

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import l.files.ui.base.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

class TextThumbnailerTest {

  private fun thumbnailer() = TextThumbnailer()

  @Test
  fun create_thumbnail_from_utf8() {
    val input: InputStream = ByteArrayInputStream("hello world".toByteArray())
    val result = thumbnailer().create(
      input,
      Rect.of(10, 999),
      getInstrumentation().context
    )!!
    val square = Rect.of(10, 10)
    assertEquals(square, Rect.of(result.bitmap()))
    assertEquals(square, result.originalSize())
  }
}
