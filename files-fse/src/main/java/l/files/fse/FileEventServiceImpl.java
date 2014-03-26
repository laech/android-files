package l.files.fse;

import android.os.FileObserver;

import com.google.common.base.Optional;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
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
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newHashMapWithExpectedSize;
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
  private final Set<String> monitored = new HashSet<>();

  /**
   * inotify operates on inodes, there could be multiple paths pointing to the
   * same inode, for example, hard links, file systems mounted on different
   * mount points (/sdcard, /storage/emulated/0, /storage/emulated/legacy,
   * /storage/sdcard0, they are all mount points of the same device).
   * <p/>
   * TODO inodes are not unique across partitions
   */
  private final Map<Long, EventObserver> observers = newHashMap();

  private final Set<FileEventListener> listeners = new CopyOnWriteArraySet<>();

  @Override public void register(FileEventListener listener) {
    listeners.add(listener);
  }

  @Override public void unregister(FileEventListener listener) {
    listeners.remove(listener);
  }

  @Override public void stopAll() {
    synchronized (this) {
      for (FileObserver observer : observers.values()) {
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
      for (EventObserver observer : observers.values()) {
        if (observer.getPaths().contains(path)) {
          return true;
        }
      }
      return false;
    }
  }

  private String getNormalizedPath(File file) {
    return new File(file.toURI().normalize()).getAbsolutePath();
  }

  @Override public Optional<Map<File, Stat>> monitor(File file) {
    long inode;
    try {
      inode = stat(file.getPath()).ino;
    } catch (OsException e) {
      // TODO
      throw new RuntimeException(file.toString(), e);
    }
    String path = getNormalizedPath(file);
    synchronized (this) {
      if (!monitored.add(path)) {
        return Optional.absent();
      }
      startObserver(path, inode);
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
        startObserver(child.getPath(), stat.ino);
      }
    }
    return Optional.of(stats);
  }

  private void startObserver(String path, long inode) {
    synchronized (this) {

      EventObserver observer = observers.get(inode);
      boolean newObserver = (observer == null);
      if (newObserver) {
        observer = new EventObserver(path, MODIFICATION_MASK);
        observer.addListeners(new StopSelfListener(observer, this));
        observers.put(inode, observer);
      }

      observer.addPath(path);
      observer.addListeners(
          new UpdateSelfListener(path, this),
          new UpdateChildrenListener(path, this));

      if (newObserver) {
        observer.startWatching();
        logger.debug("Started new observer %s", path);
      }
    }
  }

  @Override public void onObserverStopped(EventObserver observer) {
    logger.debug("Stopped observer %s", observer.getPath());
    synchronized (this) {
      monitored.removeAll(observer.getPaths());
      observers.values().remove(observer);
      for (String location : observer.getPaths()) {
        String prefix = location.endsWith("/") ? location : location + "/";
        removeMonitored(prefix);
        removeObservers(prefix);
      }
    }
  }

  private void removeMonitored(String pathPrefix) {
    Iterator<String> it = monitored.iterator();
    while (it.hasNext()) {
      if (it.next().startsWith(pathPrefix)) {
        it.remove();
      }
    }
  }

  private void removeObservers(String pathPrefix) {
    Iterator<EventObserver> it = observers.values().iterator();
    while (it.hasNext()) {
      EventObserver observer = it.next();
      for (String path : observer.getPaths()) {
        if (path.startsWith(pathPrefix)) {
          observer.removePath(path);
          if (observer.getPaths().isEmpty()) {
            observer.stopWatching();
            it.remove();
            logger.debug("Stopping observer %s", observer.getPath());
          }
        }
      }
    }
  }

  private Stat stat(String path) throws OsException {
    // Use stat() instead of lstat() as stat()
    // lstat() returns the inode of the link
    // stat() returns the inode of the referenced file/directory
    return Stat.stat(path);
  }

  @Override public void onFileChanged(int event, String parent, String path) {
    if (isMonitored(parent)) {
      for (FileEventListener listener : listeners) {
        listener.onFileChanged(event, parent, path);
      }
    }
  }

  @Override public void onFileAdded(int event, String parent, String child) {
    String path = parent + "/" + child;
    File file = new File(path);
    if (file.isDirectory() && isMonitored(parent)) {
      try {
        startObserver(path, stat(path).ino);
      } catch (OsException e) {
        logger.warn(e, "Failed to stat %s", path);
      }
    }
    for (FileEventListener listener : listeners) {
      listener.onFileAdded(event, parent, child);
    }
  }

  @Override public void onFileRemoved(int event, String parent, String child) {
    String path = parent + "/" + child;
    synchronized (this) {
      Iterator<EventObserver> it = observers.values().iterator();
      while (it.hasNext()) {
        EventObserver observer = it.next();
        if (observer.getPaths().contains(path)) {
          observer.removePath(path);
          if (observer.getPaths().isEmpty()) {
            observer.stopWatching();
            it.remove();
          }
          break;
        }
      }
    }
    for (FileEventListener listener : listeners) {
      listener.onFileRemoved(event, parent, child);
    }
  }
}
