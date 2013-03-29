package com.example.files.app;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import com.example.files.R;
import com.example.files.event.ShowHiddenFilesEvent;
import com.squareup.otto.Bus;
import junit.framework.TestCase;

public final class PreferenceChangeNotifierTest extends TestCase {

  private PreferenceChangeNotifier notifier;

  private SharedPreferences preferences;
  private Resources resources;
  private Bus bus;

  @Override protected void setUp() throws Exception {
    super.setUp();
    preferences = mock(SharedPreferences.class);
    bus = mock(Bus.class);
    resources = mock(Resources.class);

    Application app = mock(Application.class);
    given(app.getResources()).willReturn(resources);

    notifier = new PreferenceChangeNotifier(app, preferences, bus);
  }

  public void testStartWillRegisterToBus() {
    notifier.start();
    verify(bus).register(notifier);
  }

  public void testStartWillRegisterToPreference() {
    notifier.start();
    verify(preferences).registerOnSharedPreferenceChangeListener(notifier);
  }

  public void testNotifiesOnShowHiddenFilesChange() {
    setShowHiddenFilesPreference("my_key", true);
    notifier.onSharedPreferenceChanged(preferences, "my_key");
    verify(bus).post(new ShowHiddenFilesEvent(true));
  }

  public void testProducesCurrentHiddenFilesEvent() {
    setShowHiddenFilesPreference("key", false);
    ShowHiddenFilesEvent event = notifier.currentShowHiddenFilesEvent();
    assertEquals(new ShowHiddenFilesEvent(false), event);
  }

  private void setShowHiddenFilesPreference(String key, boolean value) {
    given(resources.getString(R.string.pref_show_hidden_files)).willReturn(key);
    given(preferences.getBoolean(key, false)).willReturn(value);
  }
}
