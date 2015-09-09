package l.files.ui.bookmarks;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.widget.ActionModeItem;
import l.files.fs.File;
import l.files.provider.bookmarks.BookmarkManager;
import l.files.ui.selection.Selection;

import static android.view.Menu.NONE;
import static java.util.Objects.requireNonNull;

final class DeleteAction extends ActionModeItem
{
    private final BookmarkManager bookmarks;
    private final Selection<File> selections;

    DeleteAction(
            final BookmarkManager bookmarks,
            final Selection<File> selections)
    {
        super(R.id.delete_selected_bookmarks);
        this.bookmarks = requireNonNull(bookmarks, "bookmarks");
        this.selections = requireNonNull(selections, "selections");
    }

    @Override
    public boolean onCreateActionMode(final ActionMode mode, final Menu menu)
    {
        menu.add(NONE, id(), NONE, R.string.delete);
        return true;
    }

    @Override
    protected void onItemSelected(final ActionMode mode, final MenuItem item)
    {
        bookmarks.removeBookmarks(selections.copy());
        mode.finish();
    }
}
