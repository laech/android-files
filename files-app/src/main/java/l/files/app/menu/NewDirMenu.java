package l.files.app.menu;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.analytics.AnalyticsMenu;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;
import l.files.provider.FilesContract;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Menu to allow user to create a new directory on a parent directory identified
 * by the given ID.
 *
 * @see FilesContract.FileInfo#COLUMN_ID
 */
public final class NewDirMenu extends OptionsMenuAction {

  private final String parentId;
  private final FragmentManager manager;

  private NewDirMenu(FragmentManager manager, String parentId) {
    super(R.id.new_dir);
    this.manager = checkNotNull(manager, "manager");
    this.parentId = checkNotNull(parentId, "parentId");
  }

  public static OptionsMenu create(FragmentActivity activity, String parentId) {
    FragmentManager manager = activity.getSupportFragmentManager();
    OptionsMenu menu = new NewDirMenu(manager, parentId);
    return new AnalyticsMenu(activity, menu, "new_dir");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, R.string.new_dir)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override protected void onItemSelected(MenuItem item) {
    NewDirFragment.create(parentId).show(manager, NewDirFragment.TAG);
  }
}
