package l.files.app;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.io.File;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.UserDirs.*;
import static l.files.setting.Settings.*;
import static l.files.sort.Sorters.NAME;

public final class FilesApp extends Application {

  // TODO
  private static final Set<File> DEFAULT_BOOKMARKS = ImmutableSet.of(
      DIR_DCIM,
      DIR_MUSIC,
      DIR_MOVIES,
      DIR_PICTURES,
      DIR_DOWNLOADS);

  public static Bus getBus(Fragment fragment) {
    return getBus(fragment.getActivity());
  }

  public static Bus getBus(Context context) {
    return ((FilesApp) context.getApplicationContext()).bus;
  }

  private Bus bus;

  @Override public void onCreate() {
    super.onCreate();

    bus = new Bus(ThreadEnforcer.MAIN);
    SharedPreferences pref = getDefaultSharedPreferences(this);
    registerSortProvider(bus, pref, NAME);
    registerBookmarksProvider(bus, pref, DEFAULT_BOOKMARKS);
    registerShowHiddenFilesProvider(bus, pref, false);

    if (DEBUG) {
      StrictMode.enableDefaults();
    }
  }
}
