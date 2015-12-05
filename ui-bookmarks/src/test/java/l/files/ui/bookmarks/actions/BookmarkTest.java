package l.files.ui.bookmarks.actions;

import android.view.Menu;
import android.view.MenuItem;

import org.junit.Before;
import org.junit.Test;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.Path;
import l.files.ui.bookmarks.R;

import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class BookmarkTest {

    private Path file;
    private Bookmark action;
    private BookmarkManager bookmarks;

    @Before
    public void setUp() throws Exception {
        file = mock(Path.class, "bookmark");
        bookmarks = mock(BookmarkManager.class);
        action = new Bookmark(file, bookmarks);
    }

    @Test
    public void on_create_makes_checkable_bookmark_menu_item() {

        MenuItem item = mock(MenuItem.class);
        Menu menu = mock(Menu.class);
        given(menu.add(anyInt(), anyInt(), anyInt(), anyInt())).willReturn(item);

        action.onCreateOptionsMenu(menu);

        verify(menu).add(anyInt(), eq(R.id.bookmark), anyInt(), eq(R.string.bookmark));
        verify(item).setCheckable(true);
    }

    @Test
    public void on_prepare_checks_menu_item_if_file_is_bookmarked() {
        testOnPrepareUpdatesMenuItemCheckState(true);
    }

    @Test
    public void on_prepare_unchecks_menu_item_if_file_is_not_bookmarked() {
        testOnPrepareUpdatesMenuItemCheckState(false);
    }

    private void testOnPrepareUpdatesMenuItemCheckState(boolean bookmarked) {

        MenuItem item = mock(MenuItem.class);
        Menu menu = mock(Menu.class);
        given(menu.findItem(R.id.bookmark)).willReturn(item);
        given(bookmarks.hasBookmark(file)).willReturn(bookmarked);

        action.onPrepareOptionsMenu(menu);

        verify(item).setChecked(bookmarked);
    }

    @Test
    public void on_click_adds_file_to_bookmarks_if_file_is_not_already_bookmarked() {
        testOnClickBookmark(false);
    }

    @Test
    public void on_click_removes_file_from_bookmarks_if_file_is_already_bookmarked() {
        testOnClickBookmark(true);
    }

    private void testOnClickBookmark(boolean alreadyBookmarkedBeforeClick) {
        MenuItem item = mock(MenuItem.class);
        given(item.isChecked()).willReturn(alreadyBookmarkedBeforeClick);

        action.onItemSelected(item);

        verify(item).setChecked(!alreadyBookmarkedBeforeClick);

        if (alreadyBookmarkedBeforeClick) {
            verify(bookmarks).removeBookmark(file);
        } else {
            verify(bookmarks).addBookmark(file);
        }
    }

}
