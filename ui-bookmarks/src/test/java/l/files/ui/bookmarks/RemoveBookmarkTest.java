package l.files.ui.bookmarks;

import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.view.ActionMode;
import l.files.bookmarks.BookmarksManager;
import l.files.fs.Path;
import l.files.ui.base.selection.Selection;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public final class RemoveBookmarkTest {

    private ActionMode mode;
    private RemoveBookmark action;
    private BookmarksManager bookmarks;
    private Selection<Path, Path> selection;

    @Before
    public void setUp() {
        mode = mock(ActionMode.class);
        selection = new Selection<>();
        bookmarks = mock(BookmarksManager.class);
        action = new RemoveBookmark(selection, bookmarks);
    }

    @Test
    public void on_create_makes_remove_bookmark_menu_item() {

        MenuItem item = mock(MenuItem.class);
        Menu menu = mock(Menu.class);
        given(menu.add(
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt()
        )).willReturn(item);

        action.onCreateActionMode(mode, menu);

        verify(menu).add(
                anyInt(),
                eq(R.id.delete_selected_bookmarks),
                anyInt(),
                eq(R.string.remove)
        );
    }

    @Test
    public void on_click_removes_selected_bookmarks() {

        MenuItem item = mock(MenuItem.class);
        Path a = mock(Path.class);
        Path b = mock(Path.class);
        selection.add(a, a);
        selection.add(b, b);

        action.onItemSelected(mode, item);

        verify(bookmarks).removeAll(selection.keys());
        verifyNoMoreInteractions(bookmarks);
    }

    @Test
    public void on_click_finishes_action_mode() {
        MenuItem item = mock(MenuItem.class);
        action.onItemSelected(mode, item);
        verify(mode).finish();
    }

}
