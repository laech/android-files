package com.example.files.util;

import android.os.SystemClock;
import android.util.Log;

import static com.example.files.BuildConfig.DEBUG;

public final class DebugTimer {

  public static DebugTimer start(String tag) {
    DebugTimer timer = new DebugTimer(tag);
    timer.start();
    return timer;
  }

  private final String tag;
  private long start;

  private DebugTimer(String tag) {
    this.tag = tag;
  }

  public void start() {
    if (DEBUG) start = now();
  }

  private long now() {
    return SystemClock.elapsedRealtime();
  }

  public void log(Object label) {
    if (DEBUG) doLog(label);
  }

  public void log(Object label1, Object label2) {
    if (DEBUG) doLog(label1 + ", " + label2);
  }

  private void doLog(Object label) {
    Log.d(tag, label + ": " + (now() - start) + " ms");
  }
}
