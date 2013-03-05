package com.example.files;

import android.app.Application;
import android.app.Fragment;
import android.content.Context;
import dagger.ObjectGraph;

public final class FilesApp extends Application {

  public static void inject(Context o) {
    ((FilesApp)o.getApplicationContext()).injector.inject(o);
  }

  public static void inject(Fragment o) {
    ((FilesApp)o.getActivity().getApplication()).injector.inject(o);
  }

  private ObjectGraph injector;

  @Override public void onCreate() {
    super.onCreate();
    injector = ObjectGraph.create(new FilesModule());
  }
}
