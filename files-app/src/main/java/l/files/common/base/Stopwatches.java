package l.files.common.base;

import com.google.common.base.Stopwatch;

import javax.annotation.Nullable;

import static l.files.BuildConfig.DEBUG;

public final class Stopwatches {
  private Stopwatches() {
  }

  @Nullable public static Stopwatch startWatchIfDebug() {
    if (DEBUG) {
      return Stopwatch.createStarted();
    }
    return null;
  }

}

