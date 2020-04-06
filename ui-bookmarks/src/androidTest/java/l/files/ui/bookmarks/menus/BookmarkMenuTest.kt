package l.files.ui.bookmarks.menus

import android.view.Menu
import android.view.MenuItem
import androidx.test.annotation.UiThreadTest
import l.files.base.lifecycle.CollectionLiveData.Companion.setLiveData
import l.files.base.lifecycle.SetLiveData
import l.files.fs.Path
import l.files.ui.bookmarks.R
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class BookmarkMenuTest {

    private lateinit var file: Path
    private lateinit var action: BookmarkMenu
    private lateinit var bookmarks: SetLiveData<Path>

    @Before
    fun setUp() {
        file = mock(Path::class.java, "bookmark")
        bookmarks = setLiveData()
        action = BookmarkMenu(file, bookmarks)
    }

    @Test
    fun on_create_makes_checkable_bookmark_menu_item() {
        val item = mock(MenuItem::class.java)
        val menu = mock(Menu::class.java)
        given(menu.add(anyInt(), anyInt(), anyInt(), anyInt())).willReturn(item)
        action.onCreateOptionsMenu(menu)
        verify(menu).add(
            anyInt(),
            eq(R.id.bookmark),
            anyInt(),
            eq(R.string.bookmark)
        )
        verify(item).isCheckable = true
    }

    @Test
    @UiThreadTest
    fun on_prepare_checks_menu_item_if_file_is_bookmarked() {
        testOnPrepareUpdatesMenuItemCheckState(true)
    }

    @Test
    @UiThreadTest
    fun on_prepare_unchecks_menu_item_if_file_is_not_bookmarked() {
        testOnPrepareUpdatesMenuItemCheckState(false)
    }

    private fun testOnPrepareUpdatesMenuItemCheckState(bookmarked: Boolean) {
        val item = mock(MenuItem::class.java)
        val menu = mock(Menu::class.java)
        given(menu.findItem(R.id.bookmark)).willReturn(item)
        if (bookmarked) {
            bookmarks.add(file)
        }
        action.onPrepareOptionsMenu(menu)
        verify(item).isChecked = bookmarked
    }

    @Test
    @UiThreadTest
    fun on_click_adds_file_to_bookmarks_if_file_is_not_already_bookmarked() {
        testOnClickBookmark(false)
    }

    @Test
    fun on_click_removes_file_from_bookmarks_if_file_is_already_bookmarked() {
        testOnClickBookmark(true)
    }

    private fun testOnClickBookmark(alreadyBookmarkedBeforeClick: Boolean) {
        val item = mock(MenuItem::class.java)
        given(item.isChecked).willReturn(alreadyBookmarkedBeforeClick)
        action.onItemSelected(item)

        verify(item).isChecked = !alreadyBookmarkedBeforeClick
        assertThat(
            bookmarks.contains(file),
            equalTo(!alreadyBookmarkedBeforeClick)
        )
    }
}
