package l.files.fs.media

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import l.files.testing.fs.PathBaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.nio.file.Files.*
import java.nio.file.Path

class DetectorTest : PathBaseTest() {

  private val context: Context
    get() = InstrumentationRegistry.getInstrumentation().context

  @Test
  fun can_detect_by_name() {
    val file = createTextFile("a.txt", "")
    assertEquals("text/plain", Detector.detect(context, file))
  }

  @Test
  fun can_detect_by_content() {
    val file = createTextFile("a.png")
    assertEquals("text/plain", Detector.detect(context, file))
  }

  @Test
  fun detects_directory_type() {
    val dir = createDir("b")
    assertEquals("inode/directory", Detector.detect(context, dir))
  }

  @Test
  fun detects_file_type() {
    val file = createTextFile("a.txt")
    assertEquals("text/plain", Detector.detect(context, file))
  }

  @Test
  fun detects_file_type_uppercase_extension() {
    val file = createTextFile("a.TXT")
    assertEquals("text/plain", Detector.detect(context, file))
  }

  @Test
  fun detects_linked_file_type() {
    val file = createTextFile("a.mp3")
    val link = createSymbolicLink(dir1().resolve("b.txt"), file)
    assertEquals("text/plain", Detector.detect(context, link))
  }

  @Test
  fun detects_linked_directory_type() {
    val dir = createDir("a")
    val link = createSymbolicLink(dir1().resolve("b"), dir)
    assertEquals("inode/directory", Detector.detect(context, link))
  }

  @Test
  fun detects_multi_linked_directory_type() {
    val dir = createDir("a")
    val link1 = createSymbolicLink(dir1().resolve("b"), dir)
    val link2 = createSymbolicLink(dir1().resolve("c"), link1)
    assertEquals("inode/directory", Detector.detect(context, link2))
  }

  @Test
  fun fails_on_broken_circular_links() {
    val link1 = dir1().resolve("link1")
    val link2 = dir1().resolve("link2")
    createSymbolicLink(link1, link2)
    createSymbolicLink(link2, link1)
    try {
      Detector.detect(context, link1)
      fail()
    } catch (e: IOException) {
      // Pass
    }
  }

  private fun createDir(name: String): Path =
    createDirectory(dir1().resolve(name))

  private fun createTextFile(
    name: String,
    content: String = "hello world",
  ): Path {
    val path = dir1().resolve(name)
    write(path, content.toByteArray())
    return path
  }
}
