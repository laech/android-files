package l.files.app;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;

public final class Preferences {

  private static final String PREF_SORT_ORDER = "sort_order";
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

  public static boolean isSortOrderKey(String key) {
    return PREF_SORT_ORDER.equals(key);
  }

  public static boolean getShowHiddenFiles(Context context) {
    return get(context).getBoolean(PREF_SHOW_HIDDEN_FILES, false);
  }

  public static String getSortOrder(Context context) {
    return get(context).getString(PREF_SORT_ORDER, SORT_BY_NAME);
  }

  public static void setShowHiddenFiles(Context context, boolean show) {
    get(context).edit().putBoolean(PREF_SHOW_HIDDEN_FILES, show).apply();
  }

  public static void setSortOrder(Context context, String sortOrder) {
    get(context).edit().putString(PREF_SORT_ORDER, sortOrder).apply();
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
