package l.files.ui;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

import l.files.ui.analytics.Analytics;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.provider.FilesContract.Files.SORT_BY_MODIFIED;
import static l.files.provider.FilesContract.Files.SORT_BY_NAME;
import static l.files.provider.FilesContract.Files.SORT_BY_SIZE;

public final class Preferences {

  private static final String PREF_SORT_ORDER = "sort_order";
  private static final String PREF_SHOW_HIDDEN_FILES = "show_hidden_files";
  private static final String PREF_SHOW_PATH_BAR = "show_path_bar";

  /**
   * Future proofs the sort order, i.e. if what is saved in the preference is no
   * longer valid in a newer version of the app, instead of blowing up, just
   * return the latest default sort order instead.
   */
  private static final Set<String> VALID_SORTS = ImmutableSet.of(
      SORT_BY_NAME,
      SORT_BY_MODIFIED,
      SORT_BY_SIZE
  );

  /**
   * Returns a preference listener that can be used to track preference change
   * events. Only one listener should ever be registered, otherwise duplicate
   * events will be tracked.
   */
  static OnSharedPreferenceChangeListener newAnalyticsListener(
      final Context context) {
    return new OnSharedPreferenceChangeListener() {
      @Override public void onSharedPreferenceChanged(
          SharedPreferences pref, String key) {
        String value;
        switch (key) {
          case PREF_SHOW_HIDDEN_FILES:
          case PREF_SHOW_PATH_BAR:
            value = Boolean.toString(get(context).getBoolean(key, false));
            break;
          case PREF_SORT_ORDER:
            value = get(context).getString(key, null);
            break;
          default:
            return;
        }
        Analytics.onPreferenceChanged(context, key, value);
      }
    };
  }

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
    String value = get(context).getString(PREF_SORT_ORDER, SORT_BY_NAME);
    return VALID_SORTS.contains(value) ? value : SORT_BY_NAME;
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
