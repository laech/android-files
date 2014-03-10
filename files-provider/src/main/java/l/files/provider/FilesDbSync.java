package l.files.provider;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.FileObserver;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import l.files.os.Os;

import static android.os.FileObserver.ATTRIB;
import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MODIFY;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Map.Entry;
import static l.files.provider.FilesContract.getFileLocation;

final class FilesDbSync implements
    StopSelfListener.Callback,
    UpdateSelfListener.Callback,
    UpdateChildrenListener.Callback {

  // TODO monitor on inode instead, and test this

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


  /**
   * inotify operates on inodes, there could be multiple paths pointing to the
   * same inode, for example, hard links, file systems mounted on different
   * mount points (/sdcard, /storage/emulated/0, /storage/emulated/legacy,
   * /storage/sdcard0, they are all mount points of the same device).
   * TODO inodes are not unique across partitions
   * TODO simply this, too many maps for keeping states
   */
  private final Map<Long, DirWatcher> observersByInode = newHashMap();
  private final Map<String, DirWatcher> observersByLocation = new ConcurrentHashMap<>();
  private final Multimap<String, DirWatcherListener> locationListeners = HashMultimap.create();
  private final Multimap<DirWatcher, String> locations = HashMultimap.create();

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
      for (FileObserver observer : observersByLocation.values()) {
        observer.stopWatching();
      }
      observersByLocation.clear();
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
    long inode = Os.inode(file.getPath());
    if (inode == -1) {
      return true; // TODO handle this
    }
    String location = getFileLocation(file);
    synchronized (this) {
      if (!monitored.add(location)) {
        return false;
      }

      DirWatcher observer = observersByInode.get(inode);
      boolean newObserver = (observer == null);
      if (newObserver) {
        observer = new DirWatcher(file, MODIFICATION_MASK); // TODO this is only the initial file
        observersByInode.put(inode, observer);
        observersByLocation.put(location, observer);
      }
      Processor processor = new Processor(helper, context.getContentResolver());
      Collection<DirWatcherListener> listeners = Arrays.<DirWatcherListener>asList(
          new StopSelfListener(observer, this),
          new UpdateSelfListener(context, file, processor, helper, this),
          new UpdateChildrenListener(context, file, processor, helper, this));
      observer.addListeners(listeners);
      locationListeners.putAll(location, listeners);
      locations.put(observer, location);
      if (newObserver) {
        observer.startWatching();
        if (listener != null) {
          listener.onStart(observer);
        }
      }
      return true;
    }
  }

  @Override public void onObserverStopped(DirWatcher observer) {
    synchronized (this) {
      for (String location : locations.removeAll(observer)) {
        observersByLocation.remove(location);
        monitored.remove(location);
        observersByInode.values().remove(observer);
        String prefix = location.endsWith("/") ? location : location + "/";
        removeMonitored(prefix);
        removeObservers(prefix);
      }
    }
  }

  private void removeMonitored(String locationPrefix) {
    Iterator<String> it = monitored.iterator();
    while (it.hasNext()) {
      String location = it.next();
      if (location.startsWith(locationPrefix)) {
        it.remove();
      }
    }
  }

  private void removeObservers(String locationPrefix) {
    Iterator<Entry<String, DirWatcher>> it = observersByLocation.entrySet().iterator();
    while (it.hasNext()) {
      Entry<String, DirWatcher> entry = it.next();
      String location = entry.getKey();
      if (location.startsWith(locationPrefix)) {
        DirWatcher observer = entry.getValue();
        observer.removeListeners(locationListeners.removeAll(location));
        locations.remove(observer, location);
        if (locations.get(observer).isEmpty()) {
          observer.stopWatching();
          observersByInode.values().remove(observer);
          it.remove();
          if (listener != null) {
            listener.onStop(observer);
          }
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
      DirWatcher observer = observersByLocation.get(location);
      if (observer != null) {
        locations.remove(observer, location);
        locationListeners.removeAll(location);
        if (locations.get(observer).isEmpty()) {
          observer.stopWatching();
          observersByLocation.remove(location);
          observersByInode.values().remove(observer);
        }
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
