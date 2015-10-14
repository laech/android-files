package l.files.ui.bookmarks.actions;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import l.files.bookmarks.BookmarkManager;
import l.files.fs.File;
import l.files.ui.base.app.OptionsMenuAction;
import l.files.ui.bookmarks.R;

import static android.view.Menu.NONE;
import static java.util.Objects.requireNonNull;

public final class Bookmark extends OptionsMenuAction {

    private final BookmarkManager bookmarks;
    private final File file;

    public Bookmark(File file, Context context) {
        this(file, BookmarkManager.get(context));
    }

    Bookmark(File file, BookmarkManager bookmarks) {
        super(R.id.bookmark);
        this.bookmarks = requireNonNull(bookmarks, "bookmarks");
        this.file = requireNonNull(file, "file");
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
            item.setChecked(bookmarks.hasBookmark(file));
        }
    }

    @Override
    protected void onItemSelected(MenuItem item) {
        boolean checked = item.isChecked();
        item.setChecked(!checked);
        if (checked) {
            bookmarks.removeBookmark(file);
        } else {
            bookmarks.addBookmark(file);
        }
    }

}
