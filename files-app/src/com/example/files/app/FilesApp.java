package com.example.files.app;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.VmPolicy;
import static android.os.StrictMode.setThreadPolicy;
import static android.os.StrictMode.setVmPolicy;
import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.example.files.BuildConfig.DEBUG;

import android.app.Application;
import android.content.Context;
import android.support.v4.app.Fragment;
import com.example.files.util.DebugTimer;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

public final class FilesApp extends Application {

  private static final String TAG = FilesApp.class.getSimpleName();

  public static final Bus BUS = new Bus(ThreadEnforcer.MAIN);

  public static FilesApp getApp(Context context) {
    return (FilesApp) context.getApplicationContext();
  }

  public static FilesApp getApp(Fragment fragment) {
    return getApp(fragment.getActivity());
  }

  private Settings settings;

  @Override public void onCreate() {
    DebugTimer timer = DebugTimer.start(TAG);
    super.onCreate();
    settings = new Settings(this, getDefaultSharedPreferences(this));
    setStrictModeIf(DEBUG);
    timer.log("FilesApp.onCreate");
  }

  public Settings getSettings() {
    return settings;
  }

  private void setStrictModeIf(boolean set) {
    if (set) {
      setThreadPolicy(
          new ThreadPolicy.Builder().detectAll().penaltyLog().build());
      setVmPolicy(
          new VmPolicy.Builder().detectAll().penaltyLog().build());
    }
  }
}
