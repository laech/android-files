package l.files.ui.menu;

import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.common.app.OptionsMenuAction;
import l.files.fs.Path;
import l.files.provider.bookmarks.BookmarkManager;

import static android.view.Menu.NONE;
import static java.util.Objects.requireNonNull;

public final class BookmarkMenu extends OptionsMenuAction {

  private final BookmarkManager bookmarks;
  private final Path path;

  public BookmarkMenu(BookmarkManager bookmarks, Path path) {
    super(R.id.bookmark);
    this.bookmarks = requireNonNull(bookmarks);
    this.path = requireNonNull(path);
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, R.string.bookmark).setCheckable(true);
  }

  @Override public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem item = menu.findItem(id());
    if (item != null) {
      item.setChecked(bookmarks.hasBookmark(path));
    }
  }

  @Override protected void onItemSelected(MenuItem item) {
    final boolean checked = item.isChecked();
    item.setChecked(!checked);
    if (checked) {
      bookmarks.removeBookmark(path);
    } else {
      bookmarks.addBookmark(path);
    }
  }

}
