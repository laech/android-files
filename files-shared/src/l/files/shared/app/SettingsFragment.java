package l.files.shared.app;

import l.files.shared.R;
import android.os.Bundle;
import android.preference.PreferenceFragment;

public final class SettingsFragment extends PreferenceFragment {

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    addPreferencesFromResource(R.xml.preferences);
  }
}
