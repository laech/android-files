package l.files;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;
import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Bus;
import l.files.event.Events;

import java.io.File;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.BuildConfig.DEBUG;
import static l.files.event.Events.bus;
import static l.files.io.UserDirs.*;

public final class FilesApp extends Application {

  // TODO
  private static final Set<File> DEFAULT_BOOKMARKS = ImmutableSet.of(
      DIR_DCIM,
      DIR_MUSIC,
      DIR_MOVIES,
      DIR_PICTURES,
      DIR_DOWNLOADS);

  @Override public void onCreate() {
    super.onCreate();

    Bus bus = bus();
    SharedPreferences pref = getDefaultSharedPreferences(this);
    Events.registerBookmarksProvider(bus, pref, DEFAULT_BOOKMARKS);
    Events.registerViewProvider(bus, pref);

    if (DEBUG) {
      StrictMode.enableDefaults();
    }
  }
}
