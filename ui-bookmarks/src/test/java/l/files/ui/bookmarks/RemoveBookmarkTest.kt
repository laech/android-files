package l.files.ui.bookmarks

import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.view.ActionMode
import l.files.base.lifecycle.CollectionLiveData.Companion.setLiveData
import l.files.base.lifecycle.SetLiveData
import l.files.fs.Path
import l.files.ui.base.selection.Selection
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class RemoveBookmarkTest {

    private lateinit var mode: ActionMode
    private lateinit var action: RemoveBookmark
    private lateinit var bookmarks: SetLiveData<Path>
    private lateinit var selection: Selection<Path, Path>

    @Before
    fun setUp() {
        mode = mock(ActionMode::class.java)
        selection = Selection()
        bookmarks = setLiveData()
        action = RemoveBookmark(selection, bookmarks)
    }

    @Test
    fun `on create makes remove bookmark menu item`() {
        val item = mock(MenuItem::class.java)
        val menu = mock(Menu::class.java)
        given(menu.add(anyInt(), anyInt(), anyInt(), anyInt())).willReturn(item)
        action.onCreateActionMode(mode, menu)
        verify(menu).add(
            anyInt(),
            eq(R.id.delete_selected_bookmarks),
            anyInt(),
            eq(R.string.remove)
        )
    }

    @Test
    fun `on click removes selected bookmarks`() {
        val item = mock(MenuItem::class.java)
        val a = mock(Path::class.java)
        val b = mock(Path::class.java)
        selection.add(a, a)
        selection.add(b, b)
        action.onItemSelected(mode, item)
        assertThat(bookmarks.contains(a), equalTo(false))
        assertThat(bookmarks.contains(b), equalTo(false))
    }

    @Test
    fun `on click finishes action mode`() {
        val item = mock(MenuItem::class.java)
        action.onItemSelected(mode, item)
        verify(mode).finish()
    }
}