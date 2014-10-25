package l.files.ui.menu;

import android.app.Activity;
import android.view.MenuItem;

import l.files.ui.analytics.AnalyticsMenu;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Calls {@link Activity#onBackPressed()} when the action bar home button is
 * selected.
 */
public final class GoBackOnHomePressedAction extends OptionsMenuAction {

  private final Activity activity;

  private GoBackOnHomePressedAction(Activity activity) {
    super(android.R.id.home);
    this.activity = checkNotNull(activity, "activity");
  }

  public static OptionsMenu create(Activity activity) {
    OptionsMenu menu = new GoBackOnHomePressedAction(activity);
    return new AnalyticsMenu(activity, menu, "home");
  }

  @Override protected void onItemSelected(MenuItem item) {
    activity.onBackPressed();
  }
}
