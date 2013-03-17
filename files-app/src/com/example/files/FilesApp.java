package com.example.files;

import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.example.files.media.MediaMap;
import com.example.files.ui.ActivityStarter;
import com.example.files.ui.Toaster;
import com.example.files.ui.events.handlers.FileClickEventHandler;
import com.example.files.util.FileSystem;
import com.squareup.otto.Bus;

import static com.squareup.otto.ThreadEnforcer.MAIN;

public final class FilesApp extends Application {

  public static Bus getBus(Context context) {
    return ((FilesApp)context.getApplicationContext()).getBus();
  }

  public static Bus getBus(Fragment fragment) {
    return getBus(fragment.getActivity());
  }

  private Bus bus;

  public Bus getBus() {
    return bus;
  }

  @Override public void onCreate() {
    super.onCreate();
    bus = new Bus(MAIN);
    registerEventHandlers();
  }

  private void registerEventHandlers() {
    bus.register(new FileClickEventHandler(
        new FileSystem(), new MediaMap(), newActivityStarter(), newToaster())); // TODO
  }

  private ActivityStarter newActivityStarter() {
    return new ActivityStarter() {
      @Override public void startActivity(Context context, Intent intent) {
        context.startActivity(intent);
      }
    };
  }

  private Toaster newToaster() {
    return new Toaster() {
      @Override public void toast(Context context, int resId, int duration) {
        Toast.makeText(context, resId, duration).show();
      }
    };
  }
}
