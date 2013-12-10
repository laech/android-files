package l.files.app.menu;

import android.content.Context;
import android.support.v4.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import l.files.analytics.AnalyticsMenu;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAdapter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Gives a {@link ActionBarDrawerToggle} the chance to handle {@link
 * #onOptionsItemSelected(MenuItem)}.
 */
public final class ActionBarDrawerToggleAction extends OptionsMenuAdapter {

  private final ActionBarDrawerToggle toggle;

  private ActionBarDrawerToggleAction(ActionBarDrawerToggle toggle) {
    this.toggle = checkNotNull(toggle, "toggle");
  }

  public static OptionsMenu create(Context context, ActionBarDrawerToggle toggle) {
    OptionsMenu menu = new ActionBarDrawerToggleAction(toggle);
    return new AnalyticsMenu(context, menu, "action_bar_toggle");
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    return toggle.onOptionsItemSelected(item);
  }
}
