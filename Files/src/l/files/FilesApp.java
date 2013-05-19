package l.files;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.BuildConfig.DEBUG;

public class FilesApp extends Application {

  public static final Bus BUS = new Bus(ThreadEnforcer.MAIN);

  private Settings settings;

  public static FilesApp getApp(Context context) {
    return (FilesApp) context.getApplicationContext();
  }

  public static FilesApp getApp(Fragment fragment) {
    return getApp(fragment.getActivity());
  }

  @Override public void onCreate() {
    super.onCreate();
    settings = new Settings(this, getDefaultSharedPreferences(this));
    if (DEBUG) StrictMode.enableDefaults();
  }

  public Settings getSettings() {
    return settings;
  }
}
