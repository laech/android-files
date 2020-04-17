package l.files.thumbnail

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import l.files.ui.base.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

class SvgThumbnailerTest {

  @Test
  fun create_thumbnail_from_svg() {
    val input: InputStream = ByteArrayInputStream(
      """<svg fill="#000000" height="24" width="24" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"/>"""
        .toByteArray()
    )
    val originalSize = Rect.of(24, 24)
    val decodedSize = Rect.of(10, 10)
    val result = SvgThumbnailer.create(
      input,
      decodedSize,
      getInstrumentation().context
    )!!
    assertEquals(decodedSize, Rect.of(result.bitmap()))
    assertEquals(originalSize, result.originalSize())
  }

}
