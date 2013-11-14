package l.files.provider;

import android.os.FileObserver;
import android.util.Log;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.File;
import java.io.FileFilter;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.provider.BuildConfig.DEBUG;
import static l.files.provider.DirObserver.DIR_CHANGED_MASK_NO_MODIFY;
import static org.apache.commons.io.filefilter.FileFilterUtils
    .directoryFileFilter;
import static org.apache.commons.lang3.builder.ToStringStyle.MULTI_LINE_STYLE;

final class Monitor {

  private static final String TAG = Monitor.class.getSimpleName();

  private final FileObserver observer;
  private final Set<FileObserver> childObservers;
  private final Runnable listener;
  private final File directory;
  private boolean started;

  Monitor(File directory, Runnable listener) {
    this.directory = checkNotNull(directory, "directory");
    this.listener = checkNotNull(listener, "listener");
    this.observer = new DirObserver(directory, listener);
    this.childObservers = newHashSet();
    this.started = false;
  }

  /**
   * Starts this monitor if it's not already started.
   */
  void start() {
    if (started) {
      return;
    }
    started = true;
    observer.startWatching();
    startChildObservers();
    if (DEBUG) {
      Log.d(TAG, toString());
    }
  }

  private void startChildObservers() {
    File[] dirs = directory.listFiles((FileFilter) directoryFileFilter());
    if (dirs == null) {return;}
    for (File dir : dirs) {
      childObservers.add(
          new DirObserver(dir, listener, DIR_CHANGED_MASK_NO_MODIFY));
    }
    for (FileObserver childObserver : childObservers) {
      childObserver.startWatching();
    }
  }

  /**
   * Stops this monitor if it's not already stopped.
   */
  void stop() {
    if (!started) {
      return;
    }
    started = false;
    observer.stopWatching();
    for (FileObserver observer : childObservers) {
      observer.stopWatching();
    }
    if (DEBUG) {
      Log.d(TAG, toString());
    }
  }

  @Override public String toString() {
    ToStringBuilder builder = new ToStringBuilder(this, MULTI_LINE_STYLE)
        .append("listener", listener)
        .append("observer", observer);
    for (FileObserver childObserver : childObservers) {
      builder.append("childObserver", childObserver);
    }
    return builder.toString();
  }
}
