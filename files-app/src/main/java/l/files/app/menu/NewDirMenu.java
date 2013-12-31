package l.files.app.menu;

import android.app.Activity;
import android.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.analytics.AnalyticsMenu;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;

import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.provider.FilesContract.FileInfo;

/**
 * Menu to allow user to create a new directory under a parent directory
 * identified by the given {@link FileInfo#LOCATION}.
 */
public final class NewDirMenu extends OptionsMenuAction {

  private final String parentLocation;
  private final FragmentManager manager;

  private NewDirMenu(FragmentManager manager, String parentLocation) {
    super(R.id.new_dir);
    this.manager = checkNotNull(manager, "manager");
    this.parentLocation = checkNotNull(parentLocation, "parentLocation");
  }

  public static OptionsMenu create(Activity activity, String parentLocation) {
    FragmentManager manager = activity.getFragmentManager();
    OptionsMenu menu = new NewDirMenu(manager, parentLocation);
    return new AnalyticsMenu(activity, menu, "new_dir");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), NONE, R.string.new_dir)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override protected void onItemSelected(MenuItem item) {
    NewDirFragment.create(parentLocation).show(manager, NewDirFragment.TAG);
  }
}
