package l.files;

import android.app.Application;
import android.content.SharedPreferences;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableSet;
import static l.files.util.FileSystem.*;

public class Settings {

  private static final Set<String> DEFAULT_FAVORITES = ImmutableSet.of(
      getPath(DIRECTORY_DCIM),
      getPath(DIRECTORY_DOWNLOADS),
      getPath(DIRECTORY_MOVIES),
      getPath(DIRECTORY_MUSIC),
      getPath(DIRECTORY_PICTURES));

  private static String getPath(File file) {
    return file.getAbsolutePath();
  }

  private final Application application;
  private final SharedPreferences preferences;

  public Settings(Application app, SharedPreferences preferences) {
    this.application = app;
    this.preferences = preferences;
  }

  public SharedPreferences getPreferences() {
    return preferences;
  }

  public boolean shouldShowHiddenFiles() {
    return preferences.getBoolean(getShowHiddenFilesKey(), false);
  }

  public String getShowHiddenFilesKey() {
    return application.getString(R.string.pref_show_hidden_files);
  }

  public void addFavorite(File file) {
    Set<String> favorites = newHashSet(getFavorites());
    if (favorites.add(getPath(file))) {
      preferences
          .edit()
          .putStringSet(getFavoritesKey(), favorites)
          .putLong(getFavoritesUpdatedTimestampKey(), now())
          .apply();
    }
  }

  public void removeFavorite(File file) {
    Set<String> favorites = newHashSet(getFavorites());
    if (favorites.remove(getPath(file))) {
      preferences
          .edit()
          .putStringSet(getFavoritesKey(), favorites)
          .putLong(getFavoritesUpdatedTimestampKey(), now())
          .apply();
    }
  }

  private long now() {
    return currentTimeMillis();
  }

  public boolean isFavorite(File file) {
    return getFavorites().contains(getPath(file));
  }

  public Set<String> getFavorites() {
    return unmodifiableSet(preferences.getStringSet(
        getFavoritesKey(), DEFAULT_FAVORITES));
  }

  public String getFavoritesKey() {
    return application.getString(R.string.pref_favorites);
  }

  public long getFavoritesUpdatedTimestamp() {
    return preferences.getLong(getFavoritesUpdatedTimestampKey(), -1);
  }

  public String getFavoritesUpdatedTimestampKey() {
    return application.getString(R.string.pref_favorites_updated_timestamp);
  }
}