package l.files.ui.mode;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import l.files.R;
import l.files.ui.analytics.AnalyticsAction;
import l.files.common.widget.MultiChoiceModeAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Selects all the items in the list view.
 */
public final class SelectAllAction extends MultiChoiceModeAction {

  private final AbsListView list;

  private SelectAllAction(AbsListView list) {
    super(android.R.id.selectAll);
    this.list = checkNotNull(list, "list");
  }

  public static MultiChoiceModeListener create(final AbsListView list) {
    MultiChoiceModeListener action = new SelectAllAction(list);
    return new AnalyticsAction(list.getContext(), action, "select_all") {
      @Override public Long getEventValue(MenuItem item) {
        return (long) list.getCount();
      }
    };
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, android.R.string.selectAll)
        .setIcon(R.drawable.ic_action_select_all)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override protected void onItemSelected(ActionMode mode, MenuItem item) {
    int count = list.getCount();
    for (int i = 0; i < count; ++i) {
      list.setItemChecked(i, true);
    }
  }
}
