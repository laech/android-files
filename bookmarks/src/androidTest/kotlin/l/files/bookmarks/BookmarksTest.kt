package l.files.bookmarks

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.test.platform.app.InstrumentationRegistry
import l.files.testing.fs.PathBaseTest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import java.nio.file.Files.*
import java.nio.file.Paths

class BookmarksTest : PathBaseTest() {

  private lateinit var pref: SharedPreferences

  override fun setUp() {
    super.setUp()
    pref = InstrumentationRegistry
      .getInstrumentation()
      .context
      .getSharedPreferences("bookmark-test", MODE_PRIVATE)
  }

  override fun tearDown() {
    assertThat(pref.edit().clear().commit(), equalTo(true))
    super.tearDown()
  }

  @Test
  fun removes_non_existing_bookmarks() {
    val file = createFile(dir1().resolve("file"))
    val dir = createDirectory(dir1().resolve("dir"))
    val link = createSymbolicLink(dir1().resolve("link"), file)
    assertThat(
      pref.edit()
        .putStringSet(
          PREF_KEY,
          listOf(file, dir, link).map(Any::toString).toSet(),
        )
        .commit(),
      equalTo(true),
    )

    assertThat(loadBookmarks(pref), equalTo(setOf(file, link, dir)))
    delete(file)
    assertThat(loadBookmarks(pref), equalTo(setOf(dir)))
  }

  @Test
  fun collates_bookmarks_by_name() {
    val a = Paths.get("a")
    val b = Paths.get("b")
    val c = Paths.get("c")
    val d = Paths.get("d")
    val e = Paths.get("e")
    val expected = listOf(a, b, c, d, e)
    val actual = listOf(a, b, c, d, e).collate({ false })
    assertThat(actual, equalTo(expected))
  }

  @Test
  fun collate_filter_top() {
    val a = Paths.get("a")
    val z = Paths.get("z")
    val x = Paths.get("x")
    val expected = listOf(x, a, z)
    val actual = listOf(a, z, x).collate({ it == x })
    assertThat(actual, equalTo(expected))
  }
}
