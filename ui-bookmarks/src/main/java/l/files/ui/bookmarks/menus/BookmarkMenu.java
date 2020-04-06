package l.files.ui.bookmarks.menus;

import android.view.Menu;
import android.view.MenuItem;
import l.files.bookmarks.BookmarksManager;
import l.files.fs.Path;
import l.files.ui.base.app.OptionsMenuAction;
import l.files.ui.bookmarks.R;

import static android.view.Menu.NONE;
import static l.files.base.Objects.requireNonNull;

public final class BookmarkMenu extends OptionsMenuAction {

    private final BookmarksManager bookmarks;
    private final Path path;

    public BookmarkMenu(Path path, BookmarksManager bookmarks) {
        super(R.id.bookmark);
        this.bookmarks = requireNonNull(bookmarks, "bookmarks");
        this.path = requireNonNull(path, "path");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(NONE, id(), NONE, R.string.bookmark).setCheckable(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem item = menu.findItem(id());
        if (item != null) {
            item.setChecked(bookmarks.contains(path));
        }
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        boolean checked = item.isChecked();
        item.setChecked(!checked);
        if (checked) {
            bookmarks.remove(path);
        } else {
            bookmarks.add(path);
        }
    }

}
