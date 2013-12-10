package l.files.analytics;

import android.content.Context;
import android.view.MenuItem;

import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuDelegate;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tracks when the wrapped menu is selected. The event will be tracked using the
 * action identifier supplied to the constructor.
 */
public final class AnalyticsMenu extends OptionsMenuDelegate {

  private final Context context;
  private final String action;

  public AnalyticsMenu(Context context, OptionsMenu delegate, String action) {
    super(delegate);
    this.context = checkNotNull(context, "context");
    this.action = checkNotNull(action, "action");
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    boolean handled = super.onOptionsItemSelected(item);
    if (handled) {
      Analytics.onOptionsItemSelected(context, action);
    }
    return handled;
  }
}
