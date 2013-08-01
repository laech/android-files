package l.files.app.menu;

import android.view.Menu;
import android.view.MenuItem;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.R;
import l.files.app.setting.ViewOptionsEvent;
import l.files.common.app.OptionsMenuAdapter;
import l.files.app.setting.ShowHiddenFilesRequest;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

final class ShowHiddenFilesMenu
    extends OptionsMenuAdapter implements MenuItem.OnMenuItemClickListener {

  private final Bus bus;
  private Menu menu;

  ShowHiddenFilesMenu(Bus bus) {
    this.bus = checkNotNull(bus, "bus");
  }

  @Override public void onCreate(Menu menu) {
    super.onCreate(menu);
    menu.add(NONE, R.id.show_hidden_files, NONE, R.string.show_hidden_files)
        .setCheckable(true)
        .setOnMenuItemClickListener(this)
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
    bus.post(new ShowHiddenFilesRequest(!item.isChecked()));
    return true;
  }

  @Subscribe public void handle(ViewOptionsEvent event) {
    MenuItem item = menu.findItem(R.id.show_hidden_files);
    if (item != null) {
      item.setChecked(event.showHiddenFiles());
    }
  }
}
