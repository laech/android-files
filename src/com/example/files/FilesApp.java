package com.example.files;

import javax.inject.Inject;

import android.app.Application;
import android.app.Fragment;
import android.content.Context;

import com.example.files.ui.ActivityStarter;
import com.example.files.ui.Toaster;
import com.example.files.ui.events.handlers.FileClickEventHandler;
import com.example.files.util.FileSystem;
import com.squareup.otto.Bus;

import dagger.ObjectGraph;

public final class FilesApp extends Application {

  public static void inject(Context o) {
    ((FilesApp)o.getApplicationContext()).injector.inject(o);
  }

  public static void inject(Fragment o) {
    ((FilesApp)o.getActivity().getApplication()).injector.inject(o);
  }

  @Inject Bus bus;
  @Inject FileSystem fs;
  @Inject ActivityStarter starter;
  @Inject Toaster toaster;

  private ObjectGraph injector;

  @Override public void onCreate() {
    super.onCreate();
    initInjector();
    registerEventHandlers();
  }

  private void initInjector() {
    injector = ObjectGraph.create(new FilesModule());
    injector.inject(this);
  }

  private void registerEventHandlers() {
    bus.register(new FileClickEventHandler(fs, starter, toaster));
  }
}
