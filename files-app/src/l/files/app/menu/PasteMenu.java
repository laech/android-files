package l.files.app.menu;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import java.io.File;
import l.files.common.app.OptionsMenuAdapter;
import l.files.event.Clipboard;

final class PasteMenu
    extends OptionsMenuAdapter implements OnMenuItemClickListener {

  private final Bus bus;
  private final File dir;
  private MenuItem item;
  private Clipboard clipboard;

  PasteMenu(Bus bus, File dir) {
    this.bus = checkNotNull(bus, "bus");
    this.dir = checkNotNull(dir, "dir");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, android.R.id.paste, NONE, android.R.string.paste)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override public void onPrepare(Menu menu) {
    super.onPrepare(menu);
    item = menu.findItem(android.R.id.paste);
    if (null != item) item.setEnabled(false);
    bus.register(this);
    bus.unregister(this);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    if (null != clipboard) {
      bus.post(clipboard.paste(dir));
    }
    return true;
  }

  @Subscribe public void handle(Clipboard clipboard) {
    this.clipboard = clipboard;
    if (null != item) item.setEnabled(true);
  }
}
