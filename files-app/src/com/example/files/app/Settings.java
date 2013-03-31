package com.example.files.app;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import android.app.Application;
import android.content.SharedPreferences;
import com.example.files.R;
import com.example.files.util.FileSystem;
import com.google.common.collect.ImmutableSet;

public class Settings {

  private static final Set<File> DEFAULT_FAVORITES = ImmutableSet.of(
      FileSystem.DIRECTORY_DCIM,
      FileSystem.DIRECTORY_DOWNLOADS,
      FileSystem.DIRECTORY_MOVIES,
      FileSystem.DIRECTORY_MUSIC,
      FileSystem.DIRECTORY_PICTURES
  );

  private final Application application;
  private final SharedPreferences preferences;

  public Settings(Application app, SharedPreferences preferences) {
    this.application = app;
    this.preferences = preferences;
  }

  private String showHiddenFilesKey() {
    return application.getString(R.string.pref_show_hidden_files);
  }

  public boolean shouldShowHiddenFiles() {
    return preferences.getBoolean(showHiddenFilesKey(), false);
  }

  private String favoritesKey() {
    return application.getString(R.string.pref_favorites);
  }

  public Set<String> getFavoriteFilePaths() { // TODO
    return preferences.getStringSet(favoritesKey(), Collections.<String>emptySet());
  }

  public Set<File> getFavoriteFiles() { // TODO
    return DEFAULT_FAVORITES;
  }
}
