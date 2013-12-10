package l.files.analytics;

import android.content.Context;
import android.view.ActionMode;
import android.view.MenuItem;

import l.files.common.widget.MultiChoiceModeListenerDelegate;

import static android.widget.AbsListView.MultiChoiceModeListener;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Tracks when the wrapped action is selected. The event will be tracked using
 * the action identifier supplied to the constructor.
 */
public final class AnalyticsAction extends MultiChoiceModeListenerDelegate {

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
      Analytics.onMenuItemSelected(context, action);
    }
    return handled;
  }
}
