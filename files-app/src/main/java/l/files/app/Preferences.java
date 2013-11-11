package l.files.app;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.SharedPreferences
    .OnSharedPreferenceChangeListener;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;

public final class Preferences {

  private static final String PREF_SORT_ORDER = "sort_order";

  public static boolean isSortOrderKey(String key) {
    return PREF_SORT_ORDER.equals(key);
  }

  public static String getSortOrder(Context context) {
    return get(context).getString(PREF_SORT_ORDER, SORT_BY_NAME);
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
