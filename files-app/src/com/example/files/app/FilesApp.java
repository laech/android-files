package com.example.files.app;

import android.app.Application;
import android.content.Context;
import android.support.v4.app.Fragment;
import com.example.files.inject.ApplicationModule;
import com.example.files.inject.FilesModule;
import com.example.files.util.DebugTimer;
import dagger.ObjectGraph;

public final class FilesApp extends Application {

  private static final String TAG = FilesApp.class.getSimpleName();

  public static void inject(Context context, Object instance) {
    DebugTimer timer = DebugTimer.start(TAG);
    ((FilesApp) context.getApplicationContext()).graph.inject(instance);
    timer.log("inject", instance);
  }

  public static void inject(Fragment instance) {
    inject(instance.getActivity(), instance);
  }

  public static void inject(Context instance) {
    inject(instance, instance);
  }

  private ObjectGraph graph;

  @Override public void onCreate() {
    super.onCreate();
    graph = createObjectGraph();
  }

  private ObjectGraph createObjectGraph() {
    DebugTimer timer = DebugTimer.start(TAG);
    ObjectGraph graph = ObjectGraph.create(
        new ApplicationModule(this), new FilesModule());
    timer.log("createObjectGraph");
    return graph;
  }

}
