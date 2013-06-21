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

  // TODO separate out different settings object for show hidden files and favs

  private static final Set<String> DEFAULT_BOOKMARKS = ImmutableSet.of(
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
    Set<String> favorites = newHashSet(getBookmarks());
    if (favorites.add(getPath(file))) {
      preferences
          .edit()
          .putStringSet(getFavoritesKey(), favorites)
          .putLong(getFavoritesUpdatedTimestampKey(), now())
          .apply();
    }
  }

  public void removeFavorite(File file) {
    Set<String> favorites = newHashSet(getBookmarks());
    if (favorites.remove(getPath(file))) {
      preferences
          .edit()
          .putStringSet(getFavoritesKey(), favorites)
          .putLong(getFavoritesUpdatedTimestampKey(), now())
          .apply();
    }
  }

  public void setFavorite(File file, boolean favorite) {
    if (favorite) addFavorite(file);
    else removeFavorite(file);
  }

  private long now() {
    return currentTimeMillis();
  }

  public boolean isBookmark(File file) {
    return getBookmarks().contains(getPath(file));
  }

  public Set<String> getBookmarks() {
    return unmodifiableSet(preferences.getStringSet(
        getFavoritesKey(), DEFAULT_BOOKMARKS));
  }

  public String getFavoritesKey() {
    return application.getString(R.string.pref_bookmarks);
  }

  public long getFavoritesUpdatedTimestamp() {
    return preferences.getLong(getFavoritesUpdatedTimestampKey(), -1);
  }

  public String getFavoritesUpdatedTimestampKey() {
    return application.getString(R.string.pref_bookmarks_updated_timestamp);
  }
}