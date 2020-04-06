package l.files.ui.bookmarks.menus;

import android.view.Menu;
import android.view.MenuItem;
import l.files.bookmarks.BookmarksManager;
import l.files.fs.Path;
import l.files.ui.bookmarks.R;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public final class BookmarkMenuTest {

    private Path file;
    private BookmarkMenu action;
    private BookmarksManager bookmarks;

    @Before
    public void setUp() {
        file = mock(Path.class, "bookmark");
        bookmarks = mock(BookmarksManager.class);
        action = new BookmarkMenu(file, bookmarks);
    }

    @Test
    public void on_create_makes_checkable_bookmark_menu_item() {

        MenuItem item = mock(MenuItem.class);
        Menu menu = mock(Menu.class);
        given(menu.add(
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt()
        )).willReturn(item);

        action.onCreateOptionsMenu(menu);

        verify(menu).add(
                anyInt(),
                eq(R.id.bookmark),
                anyInt(),
                eq(R.string.bookmark)
        );
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
        given(bookmarks.contains(file)).willReturn(bookmarked);

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
            verify(bookmarks).remove(file);
        } else {
            verify(bookmarks).add(file);
        }
    }

}
