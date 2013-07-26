package l.files;

import android.app.Application;
import android.os.StrictMode;
import l.files.event.Events;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.BuildConfig.DEBUG;

public final class FilesApp extends Application {

  @Override public void onCreate() {
    super.onCreate();
    BookmarkHandler.register(Events.bus(), getDefaultSharedPreferences(this));

    if (DEBUG) {
      StrictMode.enableDefaults();
    }
  }

}
