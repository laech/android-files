package l.files.ui.bookmarks;

import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.File;
import l.files.ui.base.selection.Selection;
import l.files.ui.base.view.ActionModeItem;

import static android.view.Menu.NONE;
import static l.files.base.Objects.requireNonNull;

final class RemoveBookmark extends ActionModeItem {

    private final BookmarkManager bookmarks;
    private final Selection<File, ?> selections;

    RemoveBookmark(Selection<File, ?> selection, BookmarkManager bookmarks) {
        super(R.id.delete_selected_bookmarks);
        this.selections = requireNonNull(selection);
        this.bookmarks = requireNonNull(bookmarks);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        super.onCreateActionMode(mode, menu);
        menu.add(NONE, id(), NONE, R.string.remove)
                .setIcon(R.drawable.ic_remove_circle_outline_white_24dp);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        bookmarks.removeBookmarks(selections.keys());
        mode.finish();
    }

}
