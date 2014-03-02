package l.files.provider;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.FileObserver;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static android.os.FileObserver.ATTRIB;
import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MODIFY;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.provider.FilesContract.getFileLocation;

final class FilesDbSync implements
    StopSelfListener.Callback,
    UpdateSelfListener.Callback,
    UpdateChildrenListener.Callback {

  // TODO monitor on canonical file instead, and test this

  private static final int MODIFICATION_MASK = ATTRIB
      | CREATE
      | DELETE
      | DELETE_SELF
      | MODIFY
      | MOVE_SELF
      | MOVED_FROM
      | MOVED_TO;

  private TestListener listener;

  private final Set<String> monitored = new HashSet<>();
  private final Map<String, DirWatcher> observers = new ConcurrentHashMap<>();

  private final Context context;
  private final SQLiteOpenHelper helper;

  FilesDbSync(Context context, SQLiteOpenHelper helper) {
    this.context = checkNotNull(context, "context");
    this.helper = checkNotNull(helper, "helper");
  }

  void setTestListener(TestListener listener) {
    synchronized (this) {
      this.listener = listener;
    }
  }

  /**
   * Shuts down this instance. This will stop all observers. Intended for
   * testing.
   */
  void shutdown() {
    synchronized (this) {
      for (FileObserver observer : observers.values()) {
        observer.stopWatching();
      }
      observers.clear();
      monitored.clear();
    }
  }

  /**
   * Checks whether the given file is currently being monitored.
   */
  public boolean isStarted(File file) {
    synchronized (this) {
      return monitored.contains(getFileLocation(file));
    }
  }

  /**
   * Starts monitoring on the given file if hasn't already done so.
   *
   * @return true if monitoring started, false if already monitored
   */
  public boolean start(File file) {
    String location = getFileLocation(file);
    synchronized (this) {
      if (!monitored.add(location)) {
        return false;
      }
      DirWatcher observer = observers.get(location);
      if (observer == null) {
        observer = newObserver(file);
        observer.startWatching();
        observers.put(location, observer);
        if (listener != null) {
          listener.onStart(observer);
        }
      }
      return true;
    }
  }

  private DirWatcher newObserver(File dir) {
    Processor processor = new Processor(helper, context.getContentResolver());
    DirWatcher observer = new DirWatcher(dir, MODIFICATION_MASK);
    observer.setListeners(
        new StopSelfListener(observer, this),
        new UpdateSelfListener(context, dir, processor, helper, this),
        new UpdateChildrenListener(context, dir, processor, helper, this));
    return observer;
  }

  @Override public void onObserverStopped(DirWatcher observer) {
    String location = getFileLocation(observer.getDirectory());
    synchronized (this) {
      observers.remove(location);
      monitored.remove(location);
      String prefix = location.endsWith("/") ? location : location + "/";
      removeMonitored(prefix);
      removeObservers(prefix);
    }
  }

  private void removeMonitored(String locationPrefix) {
    Iterator<String> it = monitored.iterator();
    while (it.hasNext()) {
      if (it.next().startsWith(locationPrefix)) {
        it.remove();
      }
    }
  }

  private void removeObservers(String locationPrefix) {
    Iterator<Map.Entry<String, DirWatcher>> it = observers.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, DirWatcher> entry = it.next();
      if (entry.getKey().startsWith(locationPrefix)) {
        entry.getValue().stopWatching();
        it.remove();
        if (listener != null) {
          listener.onStop(entry.getValue());
        }
      }
    }
  }

  @Override public boolean onPreUpdateSelf(String parentLocation) {
    synchronized (this) {
      return monitored.contains(parentLocation);
    }
  }

  @Override public void onChildAdded(File file, String location) {
    if (file.isDirectory()) {
      start(file);
    }
  }

  @Override public void onChildRemoved(File file, String location) {
    synchronized (this) {
      DirWatcher observer = observers.remove(location);
      if (observer != null) {
        observer.stopWatching();
      }
    }
  }

  /**
   * Listener designed for tests to hook into this class.
   */
  static abstract class TestListener {

    void onStart(DirWatcher observer) {}

    void onStop(DirWatcher observer) {}
  }
}
