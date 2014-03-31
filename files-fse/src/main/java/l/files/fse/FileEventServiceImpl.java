package l.files.fse;

import android.os.FileObserver;

import com.google.common.base.Optional;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.common.logging.Logger;
import l.files.os.OsException;
import l.files.os.Stat;

import static android.os.FileObserver.ATTRIB;
import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE;
import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MODIFY;
import static android.os.FileObserver.MOVED_FROM;
import static android.os.FileObserver.MOVED_TO;
import static android.os.FileObserver.MOVE_SELF;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.os.Stat.S_ISDIR;

// TODO monitoring on root directories (/, /dev, /dev/log etc) will be flooded
// by events, and will slow everything down on the phone, especially the when
// the logs are monitored, i.e. app writes to log, app get notified log modified,
// app writes to log... infinitely...
final class FileEventServiceImpl extends FileEventService
    implements StopSelfListener.Callback, FileEventListener {

  private static final Logger logger = Logger.get(FileEventServiceImpl.class);

  private static final int MODIFICATION_MASK = ATTRIB
      | CREATE
      | DELETE
      | DELETE_SELF
      | MODIFY
      | MOVE_SELF
      | MOVED_FROM
      | MOVED_TO;

  /**
   * These are the path being monitored, via {@link #monitor(java.io.File)}.
   * <p/>
   * When a directory is being monitored, file observers are started for its
   * child directories (but child directories are not marked as monitored,
   * unless they are explicitly monitored via {@link #monitor(java.io.File)}) as
   * well to monitored changes that would change the child directories status,
   * such as adding/removing files in the child directory, which will cause the
   * child directory's last modified timestamp to be changed.
   */
  private final Set<String> monitored = newHashSet();

  /**
   * Observers operate on inodes, there could be multiple paths pointing to the
   * same inode, for example, hard links, file systems mounted on different
   * mount points (/sdcard, /storage/emulated/0, /storage/emulated/legacy,
   * /storage/sdcard0, they are all mount points of the same device).
   */
  private final List<EventObserver> observers = newArrayList();

  private final Set<FileEventListener> listeners = new CopyOnWriteArraySet<>();

  @Override public void register(FileEventListener listener) {
    listeners.add(listener);
  }

  @Override public void unregister(FileEventListener listener) {
    listeners.remove(listener);
  }

  @Override public void stopAll() {
    synchronized (this) {
      for (FileObserver observer : observers) {
        observer.stopWatching();
      }
      observers.clear();
      monitored.clear();
    }
  }

  @Override public boolean isMonitored(File file) {
    return isMonitored(getNormalizedPath(file));
  }

  private boolean isMonitored(String path) {
    synchronized (this) {
      return monitored.contains(path);
    }
  }

  @Override public boolean hasObserver(File file) {
    String path = getNormalizedPath(file);
    synchronized (this) {
      for (EventObserver observer : observers) {
        if (observer.hasPath(path)) {
          return true;
        }
      }
      return false;
    }
  }

  private String getNormalizedPath(File file) {
    return new File(file.toURI().normalize()).getAbsolutePath();
  }

  /**
   * This method checks the node in record against the given node for the path,
   * if they are different, that means the file represented by the path has been
   * deleted and recreated, but notification has yet to arrive, for example,
   * delete a file, create a directory in it's place and start monitoring on it,
   * so quickly that the file system event will be landed after the monitoring.
   * So here cleans up the current state and will handle the late events
   * appropriately in listener methods.
   */
  private void checkNode(String path, Node node) {
    for (EventObserver observer : observers) {
      if (observer.hasPath(path)) {
        if (!observer.getNode().equals(node)) {
          observer.stopWatching();
          onObserverStopped(observer);
        }
        return;
      }
    }
  }

  /**
   * Checks the observer who is monitoring on the given node, involves removing
   * any paths that no longer exists - these paths have been deleted/moved but
   * file system notifications are not arrived yet.
   */
  private EventObserver checkObserver(Node node) {
    EventObserver observer = findObserver(node);
    if (observer != null) {
      List<String> removed = observer.removeNonExistPaths();
      monitored.removeAll(removed);
      if (observer.getPathCount() == 0) {
        observer.stopWatching();
        observers.remove(observer);
        observer = null;
      }
      for (String p : removed) {
        removeChildMonitors(p);
        removeChildObservers(p);
      }
    }
    return observer;
  }

  private EventObserver findObserver(Node node) {
    for (EventObserver observer : observers) {
      if (observer.getNode().equals(node)) {
        return observer;
      }
    }
    return null;
  }

  @Override public Optional<Map<File, Stat>> monitor(File file) {
    Node node;
    try {
      node = Node.from(stat(file.getPath()));
    } catch (OsException e) {
      throw new EventException("Failed to stat " + file, e);
    }

    String path = getNormalizedPath(file);
    synchronized (this) {
      checkNode(path, node);
      if (!monitored.add(path)) {
        return Optional.absent();
      }
      startObserver(path, node);
    }
    return startObserversForSubDirs(file);
  }

  private Optional<Map<File, Stat>> startObserversForSubDirs(File file) {
    File[] children = file.listFiles();
    if (children == null) {
      return Optional.absent();
    }

    Map<File, Stat> stats = newHashMapWithExpectedSize(children.length);
    for (File child : children) {
      Stat stat;
      try {
        stat = stat(child.getPath());
      } catch (OsException e) {
        logger.warn(e, "Failed to stat %s", child);
        continue;
      }
      stats.put(child, stat);
      if (S_ISDIR(stat.mode)) {
        startObserver(child.getPath(), Node.from(stat));
      }
    }
    return Optional.of(stats);
  }

  private void startObserver(String path, Node node) {
    synchronized (this) {
      checkNode(path, node);
      EventObserver observer = checkObserver(node);
      boolean newObserver = (observer == null);
      if (newObserver) {
        observer = new EventObserver(path, node, MODIFICATION_MASK);
        observer.addListener(new StopSelfListener(observer, this, node, path));
        observers.add(observer);
      }

      observer.addPath(path);
      observer.addListener(new UpdateSelfListener(path, this));
      observer.addListener(new UpdateChildrenListener(path, this));

      if (newObserver) {
        observer.startWatching();
        logger.debug("Started new observer %s", path);
      }
    }
  }

  @Override public void onObserverStopped(EventObserver observer) {
    logger.debug("Stopped observer %s", observer);
    synchronized (this) {
      List<String> removed = observer.removePaths();
      monitored.removeAll(removed);
      observers.remove(observer);
      for (String p : removed) {
        removeChildMonitors(p);
        removeChildObservers(p);
      }
    }
  }

  private void removeChildMonitors(String parent) {
    String prefix = parent + "/";
    Iterator<String> it = monitored.iterator();
    while (it.hasNext()) {
      String path = it.next();
      if (path.startsWith(prefix)) {
        it.remove();
      }
    }
  }

  private void removeChildObservers(String parent) {
    Iterator<EventObserver> it = observers.iterator();
    while (it.hasNext()) {
      EventObserver observer = it.next();
      List<String> removed = observer.removeChildPaths(parent);
      monitored.removeAll(removed);
      if (observer.getPathCount() == 0) {
        observer.stopWatching();
        it.remove();
        logger.debug("Stopping observer %s", observer);
      }
    }
  }

  private Stat stat(String path) throws OsException {
    // Use stat() instead of lstat() as stat()
    // lstat() returns the inode of the link
    // stat() returns the inode of the referenced file/directory
    return Stat.stat(path);
  }

  @Override public void onFileChanged(int event, String parent, String child) {
    if (isMonitored(parent)) {
      for (FileEventListener listener : listeners) {
        listener.onFileChanged(event, parent, child);
      }
    }
  }

  @Override public void onFileAdded(int event, String parent, String child) {
    String path = parent + "/" + child;
    File file = new File(path);
    if (file.isDirectory() && isMonitored(parent)) {
      try {
        startObserver(path, Node.from(stat(path)));
      } catch (OsException e) {
        // Path no longer exists, permission etc, ignore and continue
        logger.warn(e, "Failed to stat %s", path);
      }
    }
    for (FileEventListener listener : listeners) {
      listener.onFileAdded(event, parent, child);
    }
  }

  @Override public void onFileRemoved(int event, String parent, String child) {
    String path = parent + "/" + child;
    if (!new File(path).exists()) {
      synchronized (this) {
        removePath(path);
      }
    }
    for (FileEventListener listener : listeners) {
      listener.onFileRemoved(event, parent, child);
    }
  }

  private void removePath(String path) {
    monitored.remove(path);
    removeChildMonitors(path);
    removeChildObservers(path);
    Iterator<EventObserver> it = observers.iterator();
    while (it.hasNext()) {
      EventObserver observer = it.next();
      if (observer.removePath(path)) {
        if (observer.getPathCount() == 0) {
          observer.stopWatching();
          it.remove();
        }
        break;
      }
    }
  }
}
