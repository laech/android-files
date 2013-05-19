package l.files.ui.app.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import l.files.R;

public final class SettingsFragment extends PreferenceFragment {

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}
