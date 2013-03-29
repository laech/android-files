package com.example.files.app;

import static android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import javax.inject.Inject;

import android.app.Application;
import android.content.SharedPreferences;
import com.example.files.R;
import com.example.files.event.ShowHiddenFilesEvent;
import com.squareup.otto.Bus;
import com.squareup.otto.Produce;

public final class PreferenceChangeNotifier
    implements OnSharedPreferenceChangeListener {

  private final Application application;
  private final SharedPreferences preferences;
  private final Bus bus;

  @Inject public PreferenceChangeNotifier(
      Application application, SharedPreferences preferences, Bus bus) {
    this.application = application;
    this.preferences = preferences;
    this.bus = bus;
  }

  public void start() {
    bus.register(this);
    preferences.registerOnSharedPreferenceChangeListener(this);
  }

  @Override public void onSharedPreferenceChanged(
      SharedPreferences pref, String key) {

    if (showHiddenFilesKey().equals(key))
      bus.post(currentShowHiddenFilesEvent());
  }

  @Produce public ShowHiddenFilesEvent currentShowHiddenFilesEvent() {
    return new ShowHiddenFilesEvent(showHiddenFilesValue());
  }

  private String showHiddenFilesKey() {
    return application.getString(R.string.pref_show_hidden_files);
  }

  private boolean showHiddenFilesValue() {
    return preferences.getBoolean(showHiddenFilesKey(), false);
  }
}
