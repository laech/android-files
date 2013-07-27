package l.files;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.StrictMode;
import com.squareup.otto.Bus;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static l.files.BuildConfig.DEBUG;
import static l.files.event.Events.bus;

public final class FilesApp extends Application {

  @Override public void onCreate() {
    super.onCreate();

    Bus bus = bus();
    SharedPreferences pref = getDefaultSharedPreferences(this);
    BookmarkHandler.register(bus, pref);
    ViewHandler.register(bus, pref);

    if (DEBUG) {
      StrictMode.enableDefaults();
    }
  }

}
