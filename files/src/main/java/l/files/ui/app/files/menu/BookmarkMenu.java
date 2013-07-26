package l.files.ui.app.files.menu;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.event.AddBookmarkRequest;
import l.files.event.BookmarksEvent;
import l.files.event.RemoveBookmarkRequest;
import l.files.ui.menu.OptionsMenuAdapter;

import java.io.File;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class BookmarkMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private Menu menu;

  private final Bus bus;
  private final File dir;

  BookmarkMenu(Bus bus, File dir) {
    this.bus = checkNotNull(bus, "bus");
    this.dir = checkNotNull(dir, "dir");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.bookmark, NONE, R.string.bookmark)
        .setOnMenuItemClickListener(this)
        .setCheckable(true)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepare(Menu menu) {
    super.onPrepare(menu);
    this.menu = menu;
    bus.register(this);
  }

  @Override public void onClose(Menu menu) {
    super.onClose(menu);
    bus.unregister(this);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    if (!item.isChecked()) {
      bus.post(new AddBookmarkRequest(dir));
    } else {
      bus.post(new RemoveBookmarkRequest(dir));
    }
    return true;
  }

  @Subscribe public void handle(BookmarksEvent event) {
    MenuItem item = menu.findItem(R.id.bookmark);
    if (item != null) item.setChecked(event.bookmarks().contains(dir));
  }

}
