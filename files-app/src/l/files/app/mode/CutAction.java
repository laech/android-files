package l.files.app.mode;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
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
import l.files.common.widget.MultiChoiceActionAdapter;
import l.files.setting.CutRequest;

final class CutAction
    extends MultiChoiceActionAdapter implements OnMenuItemClickListener {

  private final Bus bus;
  private final AbsListView list;

  private ActionMode mode;

  CutAction(AbsListView list, Bus bus) {
    this.bus = checkNotNull(bus, "bus");
    this.list = checkNotNull(list, "list");
  }

  @Override public void onCreate(ActionMode mode, Menu menu) {
    super.onCreate(mode, menu);
    this.mode = mode;
    menu.add(NONE, android.R.id.cut, NONE, android.R.string.cut)
        .setIcon(R.drawable.ic_menu_cut)
        .setOnMenuItemClickListener(this)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
  }

  @Override public boolean onMenuItemClick(MenuItem item) {
    List<File> files = getCheckedItems(list, File.class);
    if (!files.isEmpty()) bus.post(new CutRequest(files));
    if (null != mode) mode.finish();
    return true;
  }
}
