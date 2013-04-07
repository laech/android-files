package l.files.dev.app;

import l.files.shared.app.FilesApp;
import android.os.StrictMode;

public final class DevApp extends FilesApp {

  @Override public void onCreate() {
    super.onCreate();
    StrictMode.enableDefaults();
  }

}
