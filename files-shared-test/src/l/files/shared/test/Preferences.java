package l.files.shared.test;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.System.nanoTime;

import java.util.concurrent.CountDownLatch;

import l.files.shared.app.Settings;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

public final class Preferences {

  public static CountDownLatch countDownOnChange(SharedPreferences preferences) {
    final CountDownLatch latch = new CountDownLatch(1);
    preferences.registerOnSharedPreferenceChangeListener(
        new OnSharedPreferenceChangeListener() {
          @Override public void onSharedPreferenceChanged(
              SharedPreferences preferences, String key) {
            latch.countDown();
          }
        });
    return latch;
  }

  public static SharedPreferences newPreferences(Context context) {
    return context.getSharedPreferences(
        String.valueOf(nanoTime()), MODE_PRIVATE);
  }

  public static Settings newSettings(Application app, SharedPreferences p) {
    return new Settings(app, p);
  }

  private Preferences() {
  }
}
