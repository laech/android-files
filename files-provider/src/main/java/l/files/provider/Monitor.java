package l.files.provider;

import android.os.FileObserver;
import android.util.Log;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static l.files.provider.BuildConfig.DEBUG;
import static org.apache.commons.io.filefilter.FileFilterUtils.directoryFileFilter;

enum Monitor {

  INSTANCE;

  private static final String TAG = Monitor.class.getSimpleName();

  /**
   * {@link FileObserver}s for the same directory cannot coexists (regardless of
   * the event masks), stopping one will cause the internally shared observer
   * thread to be stopped, and no events will be fired to other instances.
   */
  private final Map<File, DirObserver> observers = newHashMap();

  private final SetMultimap<File, Runnable> listeners = HashMultimap.create();

  /**
   * Registers the listener to be notified when the given directory changes. Has
   * no affect if already registered.
   */
  public void register(File directory, Runnable listener) {
    synchronized (this) {
      doRegisterDir(directory, listener);
      doRegisterSubDirs(directory, listener);
    }

    if (DEBUG) {
      Log.d(TAG, toString());
    }
  }

  private void doRegisterDir(File directory, Runnable listener) {
    listeners.put(directory, listener);
    if (observers.get(directory) == null) {
      Runnable refresher = newRefresher(directory);
      observers.put(directory, startObserver(directory, refresher));
    }
  }

  /**
   * Changes in a sub directory will cause the sub directory's last updated
   * timestamp to be updated, but this event is not fired for the parent
   * directory, so listening on the sub directories for this.
   */
  private void doRegisterSubDirs(File parent, Runnable listener) {
    File[] children = parent.listFiles((FileFilter) directoryFileFilter());
    if (children != null) {
      for (File child : children) {
        doRegisterDir(child, listener);
      }
    }
  }

  /**
   * Unregisters the listener from being notified when the given directory
   * changes. Has no affect if already unregistered.
   */
  public void unregister(File directory, Runnable listener) {
    synchronized (this) {
      doUnregisterDir(directory, listener);
      doUnregisterSubDirs(directory, listener);
    }

    if (DEBUG) {
      Log.d(TAG, toString());
    }
  }

  private void doUnregisterSubDirs(File parent, Runnable listener) {
    for (File that : newArrayList(observers.keySet())) {
      if (Objects.equal(parent, that.getParentFile())) {
        doUnregisterDir(that, listener);
      }
    }
  }

  private void doUnregisterDir(File directory, Runnable listener) {
    Collection<Runnable> callbacks = listeners.get(directory);
    if (callbacks.remove(listener) && callbacks.isEmpty()) {
      observers.remove(directory).stopWatching();
    }
  }

  private DirObserver startObserver(File directory, Runnable listener) {
    DirObserver observer = new DirObserver(directory, listener);
    observer.startWatching();
    return observer;
  }

  private Runnable newRefresher(final File directory) {
    return new Runnable() {
      @Override public void run() {
        Collection<Runnable> runnables;
        synchronized (Monitor.this) {
          runnables = newArrayList(listeners.get(directory));
        }
        for (Runnable runnable : runnables) {
          runnable.run();
        }
      }
    };
  }
}
