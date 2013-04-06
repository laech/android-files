package l.files.shared.app;

import static android.os.StrictMode.setThreadPolicy;
import static android.os.StrictMode.setVmPolicy;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.shared.BuildConfig.DEBUG;
import l.files.shared.event.EventBus;
import android.app.Application;
import android.content.Context;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.support.v4.app.Fragment;

public final class FilesApp extends Application {

  public static final EventBus BUS = new EventBus();

  public static FilesApp getApp(Context context) {
    return (FilesApp) context.getApplicationContext();
  }

  public static FilesApp getApp(Fragment fragment) {
    return getApp(fragment.getActivity());
  }

  private Settings settings;

  @Override public void onCreate() {
    super.onCreate();
    settings = new Settings(this, getDefaultSharedPreferences(this));
    setStrictModeIf(DEBUG);
  }

  public Settings getSettings() {
    return settings;
  }

  private void setStrictModeIf(boolean set) {
    if (set) {
      setThreadPolicy(new ThreadPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .build());
      setVmPolicy(new VmPolicy.Builder()
          .detectAll()
          .penaltyLog()
          .build());
    }
  }
}
