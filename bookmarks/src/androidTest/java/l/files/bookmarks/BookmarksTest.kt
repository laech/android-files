package l.files.bookmarks

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.test.annotation.UiThreadTest
import androidx.test.platform.app.InstrumentationRegistry
import l.files.fs.Path
import l.files.testing.fs.PathBaseTest
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class BookmarksTest : PathBaseTest() {

    private lateinit var bookmarks: BookmarksViewModel
    private lateinit var pref: SharedPreferences

    override fun setUp() {
        super.setUp()
        pref = InstrumentationRegistry
            .getInstrumentation()
            .context
            .getSharedPreferences("bookmark-test", MODE_PRIVATE)
        bookmarks = BookmarksViewModel(lazy { pref })
    }

    override fun tearDown() {
        assertThat(pref.edit().clear().commit(), equalTo(true))
        super.tearDown()
    }


    @Test
    @UiThreadTest
    fun can_add_bookmarks() {
        val a = dir1().concat("a").createDirectory()
        val b = dir2().concat("b").createDirectory()
        bookmarks.add(a)
        bookmarks.add(b)
        assertThat(bookmarks.contains(a), equalTo(true))
        assertThat(bookmarks.contains(b), equalTo(true))
    }

    @Test
    @UiThreadTest
    fun can_remove_bookmarks() {
        val a = dir1().concat("a").createDirectory()
        val b = dir1().concat("b").createDirectory()
        val c = dir1().concat("c").createDirectory()
        bookmarks.add(a)
        bookmarks.add(b)
        bookmarks.add(c)
        bookmarks.remove(a)
        bookmarks.remove(c)
        assertThat(bookmarks.contains(a), equalTo(false))
        assertThat(bookmarks.contains(b), equalTo(true))
        assertThat(bookmarks.contains(c), equalTo(false))
    }

    @Test
    fun removes_non_existing_bookmarks() {
        val file = dir1().concat("file").createFile()
        val dir = dir1().concat("dir").createDirectory()
        val link = dir1().concat("link").createSymbolicLink(file)
        assertThat(
            pref.edit()
                .putStringSet(PREF_KEY, encode(listOf(file, dir, link)))
                .commit(),
            equalTo(true)
        )

        assertThat(loadBookmarks(pref), equalTo(setOf(file, link, dir)))
        file.delete()
        assertThat(loadBookmarks(pref), equalTo(setOf(dir)))
    }

    @Test
    fun collates_bookmarks_by_name() {
        val a = Path.of("a")
        val b = Path.of("b")
        val c = Path.of("c")
        val d = Path.of("d")
        val e = Path.of("e")
        val expected = listOf(a, b, c, d, e)
        val actual = listOf(a, b, c, d, e).collate({ false })
        assertThat(actual, equalTo(expected))
    }

    @Test
    fun collate_filter_top() {
        val a = Path.of("a")
        val z = Path.of("z")
        val x = Path.of("x")
        val expected = listOf(x, a, z)
        val actual = listOf(a, z, x).collate({ it == x })
        assertThat(actual, equalTo(expected))
    }
}