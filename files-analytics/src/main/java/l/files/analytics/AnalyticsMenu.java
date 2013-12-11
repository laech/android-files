package l.files.analytics;

import android.content.Context;
import android.view.MenuItem;

import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuDelegate;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.analytics.Analytics.OnMenuItemSelectedEventProvider;

/**
 * Tracks when the wrapped menu is selected. The event will be tracked using the
 * action identifier supplied to the constructor.
 */
public class AnalyticsMenu extends OptionsMenuDelegate
    implements OnMenuItemSelectedEventProvider {

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
      String label = getEventLabel(item);
      Long value = getEventValue(item);
      Analytics.onMenuItemSelected(context, action, label, value);
    }
    return handled;
  }

  @Override public String getEventLabel(MenuItem item) {
    return null;
  }

  @Override public Long getEventValue(MenuItem item) {
    return null;
  }
}
