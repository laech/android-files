package l.files.thumbnail

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import l.files.fs.Path
import l.files.testing.fs.Paths
import l.files.ui.base.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PdfThumbnailerTest {

  private val context get() = getInstrumentation().context
  private val assets get() = context.assets

  @Test
  fun create_thumbnail_from_pdf() {
    val path = createTestPdf()
    try {
      val max = Rect.of(10, 100)
      val result = PdfThumbnailer.create(path, max, context)!!
      assertEquals(
        max.width().toLong(),
        result.bitmap().width.toLong()
      )
      assertTrue(result.originalSize().width() > max.width())
      assertTrue(result.originalSize().height() > max.height())
    } finally {
      Paths.deleteIfExists(path)
    }
  }

  private fun createTestPdf(): Path {
    val file = File.createTempFile("PdfThumbnailerTest", null)
    return try {
      val path = Path.of(file)
      openTestPdf().use { Paths.copy(it, path) }
      path
    } catch (e: Throwable) {
      assertTrue(file.delete() || !file.exists())
      throw e
    }
  }

  private fun openTestPdf() = assets.open("PdfThumbnailerTest.pdf")

}
