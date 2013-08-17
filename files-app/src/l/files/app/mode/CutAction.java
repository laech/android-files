package l.files.app.mode;

import static android.view.Menu.NONE;
import static android.view.MenuItem.OnMenuItemClickListener;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.widget.ListViews.getCheckedItems;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import java.io.File;
import java.util.Iterator;
import l.files.R;
import l.files.app.Intents;
import l.files.common.widget.MultiChoiceActionAdapter;

final class CutAction
    extends MultiChoiceActionAdapter implements OnMenuItemClickListener {

  private final ClipboardManager manager;
  private final AbsListView list;

  private ActionMode mode;

  CutAction(AbsListView list, ClipboardManager manager) {
    this.manager = checkNotNull(manager, "manager");
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
    ClipData clip = null;
    Iterator<File> it = getCheckedItems(list, File.class).iterator();
    if (it.hasNext()) {
      clip = ClipData.newIntent(null, Intents.cut(it.next()));
    }
    while (it.hasNext()) {
      clip.addItem(new ClipData.Item(Intents.cut(it.next())));
    }
    if (null != clip) manager.setPrimaryClip(clip);
    if (null != mode) mode.finish();

    return true;
  }
}
