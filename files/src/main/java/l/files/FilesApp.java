package l.files;

import android.app.Application;
import android.os.StrictMode;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import static l.files.BuildConfig.DEBUG;

public class FilesApp extends Application {

  public static final Bus BUS = new Bus(ThreadEnforcer.MAIN);

  @Override public void onCreate() {
    super.onCreate();
    if (DEBUG) StrictMode.enableDefaults();
  }

}
