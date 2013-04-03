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
import com.example.files.event.EventBus;

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
