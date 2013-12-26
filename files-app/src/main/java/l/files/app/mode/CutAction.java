package l.files.app.mode;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import com.google.common.collect.ImmutableSet;

import l.files.R;
import l.files.analytics.AnalyticsAction;
import l.files.app.Clipboards;
import l.files.common.widget.MultiChoiceModeAction;
import l.files.provider.FilesContract;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.view.MenuItem.SHOW_AS_ACTION_WITH_TEXT;
import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.app.ListViews.getCheckedFileLocations;
import static l.files.common.app.SystemServices.getClipboardManager;

/**
 * Cuts the selected files from the list view cursor.
 *
 * @see FilesContract.FileInfo
 */
public final class CutAction extends MultiChoiceModeAction {

  private final AbsListView list;
  private final ClipboardManager manager;

  private CutAction(AbsListView list, ClipboardManager manager) {
    super(android.R.id.cut);
    this.list = checkNotNull(list, "list");
    this.manager = checkNotNull(manager, "manager");
  }

  public static MultiChoiceModeListener create(AbsListView list) {
    Context context = list.getContext();
    ClipboardManager manager = getClipboardManager(context);
    MultiChoiceModeListener action = new CutAction(list, manager);
    return new AnalyticsAction(context, action, "cut");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, android.R.string.cut)
        .setIcon(R.drawable.ic_action_cut)
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM | SHOW_AS_ACTION_WITH_TEXT);
    return true;
  }

  @Override protected void onItemSelected(ActionMode mode, MenuItem item) {
    Clipboards.setCut(manager, ImmutableSet.copyOf(getCheckedFileLocations(list)));
    mode.finish();
  }
}
