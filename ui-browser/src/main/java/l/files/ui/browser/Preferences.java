package l.files.ui.browser;

import android.content.Context;
import android.content.SharedPreferences;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;

final class Preferences {

    private static final String PREF_SORT = "sort_order";
    private static final String PREF_SHOW_HIDDEN_FILES = "show_hidden_files";

    static boolean isShowHiddenFilesKey(String key) {
        return PREF_SHOW_HIDDEN_FILES.equals(key);
    }

    static boolean isSortKey(String key) {
        return PREF_SORT.equals(key);
    }

    static boolean getShowHiddenFiles(Context context) {
        return get(context).getBoolean(PREF_SHOW_HIDDEN_FILES, false);
    }

    static FileSort getSort(Context context) {
        String value = get(context).getString(PREF_SORT, FileSort.MODIFIED.name());
        try {
            return FileSort.valueOf(value);
        } catch (IllegalArgumentException e) {
            return FileSort.NAME;
        }
    }

    static void setShowHiddenFiles(Context context, boolean show) {
        get(context).edit().putBoolean(PREF_SHOW_HIDDEN_FILES, show).apply();
    }

    static void setSort(Context context, FileSort sort) {
        get(context).edit().putString(PREF_SORT, sort.name()).apply();
    }

    static void register(
            Context context, OnSharedPreferenceChangeListener listener) {
        get(context).registerOnSharedPreferenceChangeListener(listener);
    }

    static void unregister(
            Context context, OnSharedPreferenceChangeListener listener) {
        get(context).unregisterOnSharedPreferenceChangeListener(listener);
    }

    private static SharedPreferences get(Context context) {
        return getDefaultSharedPreferences(context);
    }

    private Preferences() {
    }
}
