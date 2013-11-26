package l.files.app.mode;

import android.content.ClipboardManager;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import com.google.common.collect.ImmutableSet;

import l.files.R;
import l.files.app.Clipboards;
import l.files.common.widget.SingleAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.app.ListViews.getCheckedFileIds;

final class CopyAction extends SingleAction {

  private final AbsListView list;
  private final ClipboardManager manager;

  CopyAction(AbsListView list, ClipboardManager manager) {
    this.list = checkNotNull(list, "list");
    this.manager = checkNotNull(manager, "manager");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, android.R.string.copy)
        .setIcon(R.drawable.ic_action_copy)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override protected int id() {
    return android.R.id.copy;
  }

  @Override
  protected void handleActionItemClicked(ActionMode mode, MenuItem item) {
    Clipboards.setCopy(manager, ImmutableSet.copyOf(getCheckedFileIds(list)));
    mode.finish();
  }
}
