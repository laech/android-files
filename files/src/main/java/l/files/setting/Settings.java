package l.files.setting;

import android.content.SharedPreferences;

public final class Settings {

  public static Setting<SortBy> getSortSetting(SharedPreferences pref) {
    return new SortSetting(pref);
  }

  private Settings() {}
}
