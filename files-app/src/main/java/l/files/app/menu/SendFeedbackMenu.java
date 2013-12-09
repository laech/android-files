package l.files.app.menu;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;

import l.files.R;
import l.files.app.util.GoogleFeedback;
import l.files.common.app.OptionsMenuAction;

import static android.view.Menu.CATEGORY_SECONDARY;
import static android.view.Menu.NONE;
import static android.view.MenuItem.SHOW_AS_ACTION_NEVER;
import static com.google.common.base.Preconditions.checkNotNull;

public final class SendFeedbackMenu extends OptionsMenuAction {

  private final Activity activity;

  public SendFeedbackMenu(Activity activity) {
    this.activity = checkNotNull(activity, "activity");
  }

  @Override protected int id() {
    return R.id.send_feedback;
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
