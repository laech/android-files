package l.files;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import l.files.settings.SortSetting;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.BuildConfig.DEBUG;

public class FilesApp extends Application {

  public static final Bus BUS = new Bus(ThreadEnforcer.MAIN);

  private Settings settings;
  private SortSetting sortSetting;

  public static FilesApp getApp(Context context) {
    return (FilesApp) context.getApplicationContext();
  }

  public static FilesApp getApp(Fragment fragment) {
    return getApp(fragment.getActivity());
  }

  @Override public void onCreate() {
    super.onCreate();
    SharedPreferences preferences = getDefaultSharedPreferences(this);
    settings = new Settings(this, preferences);
    sortSetting = SortSetting.create(preferences, BUS);
    if (DEBUG) StrictMode.enableDefaults();
  }

  public Settings getSettings() {
    return settings;
  }

  public SortSetting getSortSetting() {
    return sortSetting;
  }
}
