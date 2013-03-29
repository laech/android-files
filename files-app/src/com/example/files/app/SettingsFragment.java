package com.example.files.app;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.example.files.R;

public final class SettingsFragment extends PreferenceFragment {

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}
