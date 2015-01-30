package l.files.ui;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public final class Preferences {

  private static final String PREF_SORT = "sort_order";
  private static final String PREF_SHOW_HIDDEN_FILES = "show_hidden_files";
  private static final String PREF_SHOW_PATH_BAR = "show_path_bar";

  public static boolean isShowPathBarKey(String key) {
    return PREF_SHOW_PATH_BAR.equals(key);
  }

  public static boolean getShowPathBar(Context context) {
    return get(context).getBoolean(PREF_SHOW_PATH_BAR, false);
  }

  public static void setShowPathBar(Context context, boolean show) {
    get(context).edit().putBoolean(PREF_SHOW_PATH_BAR, show).apply();
  }

  public static boolean isShowHiddenFilesKey(String key) {
    return PREF_SHOW_HIDDEN_FILES.equals(key);
  }

  public static boolean isSortKey(String key) {
    return PREF_SORT.equals(key);
  }

  public static boolean getShowHiddenFiles(Context context) {
    return get(context).getBoolean(PREF_SHOW_HIDDEN_FILES, false);
  }

  public static FileSort getSort(Context context) {
    String value = get(context).getString(PREF_SORT, FileSort.NAME.name());
    try {
      return FileSort.valueOf(value);
    } catch (IllegalArgumentException e) {
      return FileSort.NAME;
    }
  }

  public static void setShowHiddenFiles(Context context, boolean show) {
    get(context).edit().putBoolean(PREF_SHOW_HIDDEN_FILES, show).apply();
  }

  public static void setSort(Context context, FileSort sort) {
    get(context).edit().putString(PREF_SORT, sort.name()).apply();
  }

  public static void register(
      Context context, OnSharedPreferenceChangeListener listener) {
    get(context).registerOnSharedPreferenceChangeListener(listener);
  }

  public static void unregister(
      Context context, OnSharedPreferenceChangeListener listener) {
    get(context).unregisterOnSharedPreferenceChangeListener(listener);
  }

  private static SharedPreferences get(Context context) {
    return getDefaultSharedPreferences(context);
  }

  private Preferences() {}
}
