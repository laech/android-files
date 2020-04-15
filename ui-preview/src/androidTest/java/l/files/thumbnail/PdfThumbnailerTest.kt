package l.files.thumbnail

import android.content.res.AssetManager
import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import l.files.fs.Path
import l.files.testing.fs.Paths
import l.files.ui.base.graphics.Rect
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PdfThumbnailerTest {

  @Test
  fun create_thumbnail_from_pdf() {
    val path = createTestPdf()
    try {
      val max = Rect.of(10, 100)
      val result =
        newThumbnailer().create(path, max, getInstrumentation().context)!!
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

  private val assets: AssetManager
    get() = getInstrumentation().context.assets

  private fun newThumbnailer() = PdfThumbnailer()
}
