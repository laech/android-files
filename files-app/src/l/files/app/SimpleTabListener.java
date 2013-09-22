package l.files.app;

import android.app.ActionBar;
import android.app.FragmentTransaction;

class SimpleTabListener implements ActionBar.TabListener {

  @Override public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {}

  @Override public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {}

  @Override public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {}
}
