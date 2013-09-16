package l.files.app.mode;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItems;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import com.squareup.otto.Bus;
import java.io.File;
import java.util.List;
import l.files.R;
import l.files.common.widget.SingleAction;
import l.files.event.CopyRequest;

final class CopyAction extends SingleAction {

  private final Bus bus;
  private final AbsListView list;

  CopyAction(AbsListView list, Bus bus) {
    this.bus = checkNotNull(bus, "bus");
    this.list = checkNotNull(list, "list");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, android.R.string.copy)
        .setIcon(R.drawable.ic_menu_copy)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override protected int id() {
    return android.R.id.copy;
  }

  @Override protected void handleActionItemClicked(ActionMode mode, MenuItem item) {
    List<File> files = getCheckedItems(list, File.class);
    if (!files.isEmpty()) {
      bus.post(new CopyRequest(files));
    }
    mode.finish();
  }
}
