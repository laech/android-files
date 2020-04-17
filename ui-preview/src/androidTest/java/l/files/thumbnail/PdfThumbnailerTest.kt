package l.files.thumbnail

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import l.files.fs.Path
import l.files.testing.fs.Paths
import l.files.ui.base.graphics.Rect
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException

class PdfThumbnailerTest {

  @Rule
  @JvmField
  val folder = TemporaryFolder()

  private val context get() = getInstrumentation().context
  private val assets get() = context.assets

  @Test
  fun create_mix_failures() {
    // https://issuetracker.google.com/issues/37016953

    val valid = createTestPdf()
    val invalid = createInvalidPdf()
    val max = Rect.of(10, 100)

    PdfThumbnailer.create(valid, max, context)

    try {
      PdfThumbnailer.create(invalid, max, context)
      fail()
    } catch (e: IOException) {
      // Pass
    }

    PdfThumbnailer.create(valid, max, context)
  }

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
    val file = folder.newFile("PdfThumbnailerTest")
    val path = Path.of(file)
    openTestPdf().use { Paths.copy(it, path) }
    return path
  }

  private fun createInvalidPdf(): Path {
    val file = folder.newFile("PdfThumbnailerTestInvalid")
    val path = Path.of(file)
    Paths.writeUtf8(path, "hello")
    return path
  }

  private fun openTestPdf() = assets.open("PdfThumbnailerTest.pdf")

}
