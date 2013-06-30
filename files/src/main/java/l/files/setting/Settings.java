package l.files.setting;

import java.io.File;

import android.content.SharedPreferences;

public final class Settings {

  public static Setting<SortBy> getSortSetting(SharedPreferences pref) {
    return new SortSetting(pref);
  }

  public static SetSetting<File> getBookmarksSetting(SharedPreferences pref) {
    return new BookmarksSetting(pref);
  }

  public static Setting<Boolean> getShowHiddenFilesSetting(SharedPreferences pref) {
    return new ShowHiddenFilesSetting(pref);
  }

  private Settings() {}
}
