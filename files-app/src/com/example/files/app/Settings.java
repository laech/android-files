package com.example.files.app;

import javax.inject.Inject;

import android.app.Application;
import android.content.SharedPreferences;
import com.example.files.R;

public class Settings {

  private final Application application;
  private final SharedPreferences preferences;

  @Inject public Settings(Application app, SharedPreferences preferences) {
    this.application = app;
    this.preferences = preferences;
  }

  private String showHiddenFilesKey() {
    return application.getString(R.string.pref_show_hidden_files);
  }

  public boolean shouldShowHiddenFiles() {
    return preferences.getBoolean(showHiddenFilesKey(), false);
  }
}
