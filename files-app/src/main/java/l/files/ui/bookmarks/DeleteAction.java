package l.files.ui.bookmarks;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.fs.Path;
import l.files.provider.bookmarks.BookmarkManager;
import l.files.ui.ListSelection;

import static android.view.Menu.NONE;
import static java.util.Objects.requireNonNull;

final class DeleteAction extends MultiChoiceModeAction {

    private final BookmarkManager bookmarks;
    private final ListSelection<Path> selections;

    public DeleteAction(BookmarkManager bookmarks, ListSelection<Path> selections) {
        super(R.id.delete_selected_bookmarks);
        this.bookmarks = requireNonNull(bookmarks, "bookmarks");
        this.selections = requireNonNull(selections, "selections");
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menu.add(NONE, id(), NONE, R.string.delete);
        return true;
    }

    @Override
    protected void onItemSelected(ActionMode mode, MenuItem item) {
        bookmarks.removeBookmarks(selections.getCheckedItems());
        mode.finish();
    }
}
