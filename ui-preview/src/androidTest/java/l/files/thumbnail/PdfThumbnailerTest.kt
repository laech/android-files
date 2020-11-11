package l.files.thumbnail

import androidx.test.platform.app.InstrumentationRegistry.getInstrumentation
import l.files.ui.base.graphics.Rect
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.IOException
import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.*
import java.nio.file.Path
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

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
      deleteIfExists(path)
    }
  }

  private fun createTestPdf(): Path {
    val path = folder.newFile("PdfThumbnailerTest").toPath()
    openTestPdf().use { copy(it, path, REPLACE_EXISTING) }
    return path
  }

  private fun createInvalidPdf(): Path {
    val path = folder.newFile("PdfThumbnailerTestInvalid").toPath()
    write(path, "hello".toByteArray(UTF_8))
    return path
  }

  private fun openTestPdf() = assets.open("PdfThumbnailerTest.pdf")

}
