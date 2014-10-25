package l.files.ui.analytics;

import android.content.Context;
import android.view.ActionMode;
import android.view.MenuItem;

import l.files.common.widget.MultiChoiceModeListenerDelegate;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.ui.analytics.Analytics.OnMenuItemSelectedEventProvider;

/**
 * Tracks when the wrapped action is selected. The event will be tracked using
 * the action identifier supplied to the constructor.
 */
public class AnalyticsAction extends MultiChoiceModeListenerDelegate
    implements OnMenuItemSelectedEventProvider {

  private final Context context;
  private final String action;

  public AnalyticsAction(
      Context context, MultiChoiceModeListener delegate, String action) {
    super(delegate);
    this.context = checkNotNull(context, "context");
    this.action = checkNotNull(action, "action");
  }

  @Override public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
    boolean handled = super.onActionItemClicked(mode, item);
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
