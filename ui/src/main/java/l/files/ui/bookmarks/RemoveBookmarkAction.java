package l.files.ui.bookmarks;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.bookmarks.BookmarkManager;
import l.files.common.widget.ActionModeItem;
import l.files.fs.File;
import l.files.ui.R;
import l.files.ui.selection.Selection;

import static android.view.Menu.NONE;
import static java.util.Objects.requireNonNull;

final class RemoveBookmarkAction extends ActionModeItem {

    private final BookmarkManager bookmarks;
    private final Selection<File> selections;

    RemoveBookmarkAction(BookmarkManager bookmarks, Selection<File> selections) {
        super(R.id.delete_selected_bookmarks);
        this.bookmarks = requireNonNull(bookmarks, "bookmarks");
        this.selections = requireNonNull(selections, "selections");
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.add(NONE, id(), NONE, R.string.remove)
                .setIcon(R.drawable.ic_remove_circle_outline_white_24dp);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        bookmarks.removeBookmarks(selections.copy());
        mode.finish();
    }

}
