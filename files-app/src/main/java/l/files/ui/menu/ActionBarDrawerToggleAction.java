package l.files.ui.menu;

import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import l.files.common.app.OptionsMenuAdapter;

import static com.google.common.base.Preconditions.checkNotNull;

public final class ActionBarDrawerToggleAction extends OptionsMenuAdapter {

  private final ActionBarDrawerToggle toggle;

  public ActionBarDrawerToggleAction(ActionBarDrawerToggle toggle) {
    this.toggle = checkNotNull(toggle);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    return toggle.onOptionsItemSelected(item);
  }
}
