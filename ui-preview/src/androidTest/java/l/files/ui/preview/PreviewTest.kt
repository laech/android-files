package l.files.ui.preview

import android.content.Context
import android.graphics.Bitmap
import androidx.test.platform.app.InstrumentationRegistry
import l.files.fs.LinkOption
import l.files.fs.Path
import l.files.fs.Stat
import l.files.testing.fs.PathBaseTest
import l.files.testing.fs.Paths
import l.files.ui.base.graphics.Rect
import org.junit.Assert.*
import org.junit.Test
import org.mockito.Mockito.mock
import java.lang.System.nanoTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class PreviewTest : PathBaseTest() {

  private lateinit var context: Context

  override fun setUp() {
    super.setUp()
    context = InstrumentationRegistry
      .getInstrumentation()
      .context
  }

  private fun newPreview() = Preview(context) {
    dir2().concat(nanoTime().toString())
  }

  @Test
  fun preview_svg() {
    testPreviewSuccessForTestFile("preview_test.svg")
  }

  @Test
  fun preview_pdf() {
    testPreviewSuccessForTestFile("preview_test.pdf")
  }

  @Test
  fun preview_m4a() {
    testPreviewSuccessForTestFile("preview_test.m4a")
  }

  @Test
  fun preview_jpg() {
    testPreviewSuccessForTestFile("preview_test.jpg")
  }

  @Test
  fun preview_png() {
    testPreviewSuccessForTestFile("preview_test.png")
  }

  @Test
  fun preview_mp4() {
    testPreviewSuccessForTestFile("preview_test.mp4")
  }

  @Test
  fun preview_apk() {
    testPreviewSuccessForTestFile("preview_test.apk")
  }

  @Test
  fun preview_plain_text() {
    testPreviewSuccessForContent("hello world")
  }

  @Test
  fun preview_xml() {
    testPreviewSuccessForContent(
      "<?xml version=\"1.0\"><hello>world</hello>"
    )
  }

  @Test
  fun preview_correctly_without_wrong_file_extension() {
    testPreviewSuccessForTestFile("preview_test.jpg", "a.pdf")
    testPreviewSuccessForTestFile("preview_test.jpg", "b.txt")
    testPreviewSuccessForTestFile("preview_test.jpg", "c")
  }

  private fun testPreviewSuccessForTestFile(
    testFile: String,
    dstFileName: String = testFile
  ) {
    val file = dir1().concat(dstFileName)
    context.assets.open(testFile).use { Paths.copy(it, file) }
    testPreviewSuccess(file)
    testPreviewSuccess(file)
  }

  @Test
  fun preview_proc_cpuinfo() {
    testPreviewSuccess(Path.of("/proc/cpuinfo"))
  }

  @Test
  fun preview_link() {
    val file = dir1().concat("file").createFile()
    val link = dir1().concat("link").createSymbolicLink(file)
    Paths.writeUtf8(file, "hi")
    testPreviewSuccess(file)
    testPreviewSuccess(file)
    testPreviewSuccess(link)
    testPreviewSuccess(link)
  }

  @Test
  fun preview_link_modified_target() {
    val file = dir1().concat("file").createFile()
    val link = dir1().concat("link").createSymbolicLink(file)
    testPreviewFailure(link)
    Paths.writeUtf8(file, "hi")
    testPreviewSuccess(link)
    testPreviewSuccess(link)
  }

  private fun testPreviewSuccessForContent(content: String) {
    val file = dir1().concat(nanoTime().toString())
    Paths.writeUtf8(file, content)
    testPreviewSuccess(file)
    testPreviewSuccess(file)
  }

  private fun testPreviewSuccess(file: Path) {
    val callback = TestCallback(1, 1)
    val stat = file.stat(LinkOption.FOLLOW)
    val preview = newPreview()
    val max = Rect.of(100, 100)
    val task = preview.get(
      file,
      stat,
      max,
      callback,
      context
    )
    assertNotNull(task)

    task!!.awaitAll(1, TimeUnit.MINUTES)
    callback.onPreviewLatch.await()
    callback.onBlurredThumbnailLatch.await()

    assertEquals(listOf(Pair(file, stat)), callback.onPreviews)
    assertEquals(listOf(Pair(file, stat)), callback.onBlurredThumbnails)

    assertNotNull(preview.getSize(file, stat, max, true))
    assertNotNull(preview.getThumbnail(file, stat, max, true))
    assertNotNull(preview.getBlurredThumbnail(file, stat, max, true))
    assertNotNull(preview.getMediaType(file, stat, max))
    assertNotNull(preview.getThumbnailFromDisk(file, stat, max))
    assertNull(preview.getNoPreviewReason(file, stat, max))
  }

  private fun testPreviewFailure(file: Path) {
    val callback = mock(Preview.Callback::class.java)
    val stat = file.stat(LinkOption.NOFOLLOW)
    val rect = Rect.of(10, 10)
    assertNull(newPreview().get(file, stat, rect, callback, context))
  }
}

private class TestCallback(
  onPreviewCount: Int,
  onBlurredThumbnailCount: Int
) : Preview.Callback {

  val onPreviewLatch = CountDownLatch(onPreviewCount)
  val onBlurredThumbnailLatch = CountDownLatch(onBlurredThumbnailCount)

  val onPreviews = ArrayList<Pair<Path, Stat>>()
  val onBlurredThumbnails = ArrayList<Pair<Path, Stat>>()

  override fun onPreviewAvailable(path: Path, stat: Stat, thumbnail: Bitmap) {
    onPreviews.add(Pair(path, stat))
    onPreviewLatch.countDown()
  }

  override fun onBlurredThumbnailAvailable(
    path: Path,
    stat: Stat,
    thumbnail: Bitmap
  ) {
    onBlurredThumbnails.add(Pair(path, stat))
    onBlurredThumbnailLatch.countDown()
  }

  override fun onPreviewFailed(path: Path, stat: Stat, cause: Any) {
    fail()
  }

}
