package l.files.app.menu;

import android.app.Activity;
import android.view.MenuItem;

import l.files.common.app.OptionsMenuAction;

import static com.google.common.base.Preconditions.checkNotNull;

public final class GoBackOnHomePressedAction extends OptionsMenuAction {

  private final Activity activity;

  public GoBackOnHomePressedAction(Activity activity) {
    super(android.R.id.home);
    this.activity = checkNotNull(activity, "activity");
  }

  @Override protected void onItemSelected(MenuItem item) {
    activity.onBackPressed();
  }
}
