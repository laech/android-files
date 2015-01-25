package l.files.ui.mode;

import android.content.ClipboardManager;
import android.content.Context;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;

import com.google.common.collect.ImmutableSet;

import l.files.common.widget.MultiChoiceModeAction;
import l.files.provider.FilesContract;
import l.files.ui.Clipboards;
import l.files.ui.analytics.AnalyticsAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_IF_ROOM;
import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.app.SystemServices.getClipboardManager;
import static l.files.common.content.res.Styles.getDrawable;
import static l.files.ui.ListViews.getCheckedFileLocations;

/**
 * Action to copy the selected files from a list view cursor.
 *
 * @see FilesContract.Files
 */
public final class CopyAction extends MultiChoiceModeAction {

  private final AbsListView list;
  private final ClipboardManager manager;

  private CopyAction(AbsListView list, ClipboardManager manager) {
    super(android.R.id.copy);
    this.list = checkNotNull(list, "list");
    this.manager = checkNotNull(manager, "manager");
  }

  public static MultiChoiceModeListener create(AbsListView list) {
    Context context = list.getContext();
    ClipboardManager manager = getClipboardManager(context);
    MultiChoiceModeListener action = new CopyAction(list, manager);
    return new AnalyticsAction(context, action, "copy");
  }

  @Override public boolean onCreateActionMode(ActionMode mode, Menu menu) {
    menu.add(NONE, id(), NONE, android.R.string.copy)
        .setIcon(getDrawable(android.R.attr.actionModeCopyDrawable, list))
        .setShowAsAction(SHOW_AS_ACTION_IF_ROOM);
    return true;
  }

  @Override protected void onItemSelected(ActionMode mode, MenuItem item) {
    Clipboards.setCopy(manager, ImmutableSet.copyOf(getCheckedFileLocations(list)));
    mode.finish();
  }
}
