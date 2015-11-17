package l.files.ui.bookmarks;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import org.junit.Before;
import org.junit.Test;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.File;
import l.files.ui.base.selection.Selection;

import static java.util.Arrays.asList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public final class RemoveBookmarkTest {

    private ActionMode mode;
    private RemoveBookmark action;
    private BookmarkManager bookmarks;
    private Selection<File> selection;

    @Before
    public void setUp() throws Exception {
        mode = mock(ActionMode.class);
        selection = new Selection<>();
        bookmarks = mock(BookmarkManager.class);
        action = new RemoveBookmark(selection, bookmarks);
    }

    @Test
    public void on_create_makes_remove_bookmark_menu_item() throws Exception {

        MenuItem item = mock(MenuItem.class);
        Menu menu = mock(Menu.class);
        given(menu.add(anyInt(), anyInt(), anyInt(), anyInt())).willReturn(item);

        action.onCreateActionMode(mode, menu);

        verify(menu).add(anyInt(), eq(R.id.delete_selected_bookmarks), anyInt(), eq(R.string.remove));
    }

    @Test
    public void on_click_removes_selected_bookmarks() throws Exception {

        MenuItem item = mock(MenuItem.class);
        File a = mock(File.class);
        File b = mock(File.class);
        selection.addAll(asList(a, b));

        action.onItemSelected(mode, item);

        verify(bookmarks).removeBookmarks(selection.copy());
        verifyNoMoreInteractions(bookmarks);
    }

    @Test
    public void on_click_finishes_action_mode() throws Exception {

        MenuItem item = mock(MenuItem.class);
        action.onItemSelected(mode, item);
        verify(mode).finish();
    }

}
