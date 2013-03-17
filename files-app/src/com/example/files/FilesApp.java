package com.example.files;

import javax.inject.Inject;

import android.app.Application;
import android.content.Context;

import com.example.files.ui.events.handlers.FileClickEventHandler;
import com.squareup.otto.Bus;

import dagger.ObjectGraph;

public final class FilesApp extends Application {

  private static volatile FilesApp instance;

  public static FilesApp getInstance() {
    return instance;
  }

  public static void inject(Context o) {
    ((FilesApp) o.getApplicationContext()).injector.inject(o);
  }

  @Inject Bus bus;
  @Inject FileClickEventHandler fileClickEventHandler;

  private ObjectGraph injector;

  @Override public void onCreate() {
    super.onCreate();
    instance = this;
    initInjector();
    registerEventHandlers();
  }

  private void initInjector() {
    injector = ObjectGraph.create(new FilesModule(), new ManifestModule());
    injector.inject(this);
  }

  private void registerEventHandlers() {
    bus.register(fileClickEventHandler);
  }
}
