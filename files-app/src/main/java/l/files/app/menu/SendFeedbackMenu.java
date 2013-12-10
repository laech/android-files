package l.files.app.menu;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.analytics.AnalyticsMenu;
import l.files.app.util.GoogleFeedback;
import l.files.common.app.OptionsMenu;
import l.files.common.app.OptionsMenuAction;

import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Menu to execute the system feedback framework to allow user to send
 * feedback.
 */
public final class SendFeedbackMenu extends OptionsMenuAction {

  private final Activity activity;

  private SendFeedbackMenu(Activity activity) {
    super(R.id.send_feedback);
    this.activity = checkNotNull(activity, "activity");
  }

  public static OptionsMenu create(Activity activity) {
    OptionsMenu menu = new SendFeedbackMenu(activity);
    return new AnalyticsMenu(activity, menu, "send_feedback");
  }

  @Override public void onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(NONE, id(), CATEGORY_SECONDARY, R.string.send_feedback)
        .setShowAsAction(SHOW_AS_ACTION_NEVER);
  }

  @Override protected void onItemSelected(MenuItem item) {
    GoogleFeedback.send(activity);
  }
}
