package l.files.app;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.v4.app.Fragment;

import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.Preferences.newAnalyticsListener;

public final class FilesApp extends Application {

  public static Bus getBus(Fragment fragment) {
    return getBus(fragment.getActivity());
  }

  public static Bus getBus(Context context) {
    return ((FilesApp) context.getApplicationContext()).bus;
  }

  private Bus bus;

  @Override
  public void onCreate() {
    super.onCreate();

    bus = new Bus(ThreadEnforcer.MAIN) {

      // TODO
      private final Set<Object> objects = newHashSet();

      @Override
      public void register(Object object) {
        if (objects.add(object)) super.register(object);
      }

      @Override
      public void unregister(Object object) {
        if (objects.remove(object)) super.unregister(object);
      }

    };

    Preferences.register(this, newAnalyticsListener(this));

    if (DEBUG) {
      StrictMode.enableDefaults();
    }
  }
}
