package l.files.app;

import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static l.files.app.Bundles.getInt;
import static l.files.app.Bundles.getStringList;

import android.app.ActionBar;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public final class ActionBars {

  private static final String STATE_TAB_COUNT = "actionBarTabCount";
  private static final String STATE_TAB_TITLES = "actionBarTabTitles";

  public static int getSavedActionBarTabCount(Bundle savedInstanceState, int defaultTabCount) {
    return Math.max(defaultTabCount,
        getInt(savedInstanceState, STATE_TAB_COUNT, defaultTabCount));
  }

  public static List<String> getSavedActionBarTabTitles(Bundle savedInstanceState) {
    return getStringList(savedInstanceState, STATE_TAB_TITLES);
  }

  public static void saveActionBarTabCount(Bundle outState, ActionBar actionBar) {
    outState.putInt(STATE_TAB_COUNT, actionBar.getTabCount());
  }

  public static void saveActionBarTabTitles(Bundle outState, ActionBar actionBar) {
    outState.putStringArrayList(STATE_TAB_TITLES, getActionBarTabTitles(actionBar));
  }

  private static ArrayList<String> getActionBarTabTitles(ActionBar actionBar) {
    ArrayList<String> titles = newArrayListWithCapacity(actionBar.getTabCount());
    for (int i = 0; i < actionBar.getTabCount(); i++) {
      CharSequence title = actionBar.getTabAt(i).getText();
      titles.add(title != null ? title.toString() : "");
    }
    return titles;
  }

  private ActionBars() {}
}
