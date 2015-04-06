package l.files.ui.menu;

import android.app.Activity;
import android.view.MenuItem;

import l.files.common.app.OptionsMenuAction;

import static java.util.Objects.requireNonNull;

public final class GoBackOnHomePressedAction extends OptionsMenuAction {

  private final Activity activity;

  public GoBackOnHomePressedAction(Activity activity) {
    super(android.R.id.home);
    this.activity = requireNonNull(activity, "activity");
  }

  @Override protected void onItemSelected(MenuItem item) {
    activity.onBackPressed();
  }
}
