package l.files.fs.local;

import com.google.common.collect.ImmutableSet;

import java.io.Closeable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.fs.FileSystemException;
import l.files.fs.Path;
import l.files.fs.WatchEvent;
import l.files.fs.WatchService;
import l.files.logging.Logger;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptyList;
import static l.files.fs.WatchEvent.Kind;
import static l.files.fs.WatchEvent.Listener;
import static l.files.fs.local.Files.checkExist;
import static l.files.fs.local.PathObserver.IN_IGNORED;
import static l.files.fs.local.android.os.FileObserver.ATTRIB;
import static l.files.fs.local.android.os.FileObserver.CLOSE_WRITE;
import static l.files.fs.local.android.os.FileObserver.CREATE;
import static l.files.fs.local.android.os.FileObserver.DELETE;
import static l.files.fs.local.android.os.FileObserver.DELETE_SELF;
import static l.files.fs.local.android.os.FileObserver.MODIFY;
import static l.files.fs.local.android.os.FileObserver.MOVED_FROM;
import static l.files.fs.local.android.os.FileObserver.MOVED_TO;
import static l.files.fs.local.android.os.FileObserver.MOVE_SELF;

public class LocalWatchService implements WatchService, Closeable {

  /*
    Note:
    Two FileObserver instances cannot be monitoring on the same inode, if one is
    stopped, the other will also be stopped, because FileObserver internally
    uses global states.
   */

  // TODO use different lock for different paths?
  // TODO should listeners be removed when a directory is deleted?
  // TODO send events in different thread to avoid blocking

  private static final Logger logger = Logger.get(LocalWatchService.class);

  private static final int MODIFICATION_MASK = ATTRIB
      | CREATE
      | DELETE
      | DELETE_SELF
      | MODIFY
      | MOVE_SELF
      | MOVED_FROM
      | MOVED_TO;

  /**
   * System directories such as /dev, /proc contain special files (some aren't
   * really files), they generate tons of file system events (MODIFY etc)
   * and they don't change. WatchService should not allow them and their sub
   * paths to be watched.
   */
  static final ImmutableSet<Path> IGNORED = ImmutableSet.<Path>of(
      LocalPath.of("/sys"),
      LocalPath.of("/proc"),
      LocalPath.of("/dev")
  );

  private static final LocalWatchService INSTANCE = new LocalWatchService(IGNORED) {
    @Override public void close() {
      throw new UnsupportedOperationException("Can't close shared instance");
    }
  };

  public static LocalWatchService create() {
    return new LocalWatchService(IGNORED);
  }

  /**
   * Gets a shared instance. The return instance cannot be closed.
   */
  public static LocalWatchService get() {
    return INSTANCE;
  }

  private final Path[] ignored;

  /**
   * These are the path being monitored, via {@link #monitor(Path)}.
   * <p/>
   * When a directory is being monitored, file observers are started for its
   * child directories (but child directories are not marked as monitored,
   * unless they are explicitly monitored via {@link #monitor(Path)}) as well to
   * monitored changes that would change the child directories status, such as
   * adding/removing files in the child directory, which will cause the child
   * directory's last modified timestamp to be changed.
   */
  private final Set<Path> monitored = newHashSet();

  /**
   * Observers operate on inodes, there could be multiple paths pointing to the
   * same inode, for example, hard links, file systems mounted on different
   * mount points (/sdcard, /storage/emulated/0, /storage/emulated/legacy,
   * /storage/sdcard0, they are all mount points of the same device).
   */
  private final List<PathObserver> observers = newArrayList();

  private final Map<Path, CopyOnWriteArraySet<Listener>> listeners = new HashMap<>();

  private final PathObserver.Listener processor = new PathObserver.Listener() {

    @Override
    public void onEvent(PathObserver observer, int event, String child) {

      for (Path path : observer.copyPaths()) { // TODO
        handleEvent(path, event, child);
      }

      if (isObserverStopped(event)) {
        onObserverStopped(observer);
      }
    }

    private void handleEvent(Path path, int event, String child) {

      if (isChildAdded(event)) {
        Path childPath = path.resolve(child);
        onCreate(childPath);
        send(Kind.CREATE, childPath);

      } else if (isChildModified(event, child)) {
        Path childPath = path.resolve(child);
        /*
         * Only send event if there is no observer running for the given child,
         * otherwise this will send an event, and then the child observer sends
         * a self modified event, resulting in duplicate events.
         */
        if (!hasObserver(childPath)) {
          send(Kind.MODIFY, childPath);
        }

      } else if (isChildDeleted(event)) {
        Path childPath = path.resolve(child);
        onDelete(childPath);
        send(Kind.DELETE, childPath);
      }

      if (isSelfModified(event, child)) {
        send(Kind.MODIFY, path);

      } else if (isSelfDeleted(event)) {
        // Check before onDelete which removes the path from the monitored list
        boolean monitored = isRegistered(path);
        onDelete(path);
        /*
         * I the parent path is monitored, an delete event would be sent for
         * this child deletion. If the parent path is not monitored and this
         * path is monitored, the code below will send the event. This way no
         * duplicate event will be sent.
         */
        if (monitored) {
          send(Kind.DELETE, path);
        }
      }
    }

    private void send(Kind kind, Path path) {
      WatchEvent event = WatchEvent.create(kind, path);
      for (Listener listener : getListeners(path)) {
        listener.onEvent(event);
      }
      Path parent = path.parent();
      if (parent != null) {
        for (Listener listener : getListeners(parent)) {
          listener.onEvent(event);
        }
      }
      logger.verbose(event);
    }

    private boolean isObserverStopped(int event) {
      return 0 != (event & IN_IGNORED);
    }

    private boolean isSelfModified(int mask, String child) {
      return (0 != (mask & ATTRIB) && child == null)
          || (0 != (mask & CREATE))
          || (0 != (mask & MOVED_TO))
          || (0 != (mask & MOVED_FROM))
          || (0 != (mask & DELETE));
    }

    private boolean isSelfDeleted(int mask) {
      return (0 != (mask & DELETE_SELF))
          || (0 != (mask & MOVE_SELF));
    }

    private boolean isChildAdded(int mask) {
      return (0 != (mask & CREATE))
          || (0 != (mask & MOVED_TO));
    }

    private boolean isChildModified(int mask, String child) {
      //noinspection SimplifiableIfStatement
      if (child == null) {
        return false;
      }
      return (0 != (mask & ATTRIB))
          || (0 != (mask & MODIFY))
          || (0 != (mask & CLOSE_WRITE));
    }

    private boolean isChildDeleted(int event) {
      return (0 != (event & MOVED_FROM))
          || (0 != (event & DELETE));
    }
  };

  LocalWatchService(Set<Path> ignored) {
    this.ignored = ignored.toArray(new Path[ignored.size()]);
  }

  @Override public void register(Path path, Listener listener) {
    LocalPath.check(path);
    if (!isWatchable(path)) {
      return;
    }

    synchronized (this) {
      if (getOrCreateListeners(path).add(listener)) {
        try {
          monitor(path);
        } catch (ErrnoException e) {
          throw e.toFileSystemException();
        }
      }
    }
  }

  private Collection<Listener> getOrCreateListeners(Path path) {
    synchronized (this) {
      CopyOnWriteArraySet<Listener> values = listeners.get(path);
      if (values == null) {
        values = new CopyOnWriteArraySet<>();
        listeners.put(path, values);
      }
      return values;
    }
  }

  private Collection<Listener> getListeners(Path path) {
    Collection<Listener> result;
    synchronized (this) {
      result = listeners.get(path);
    }
    if (result == null) {
      return emptyList();
    }
    return result;
  }

  @Override public void unregister(Path path, Listener listener) {
    LocalPath.check(path);
    synchronized (this) {
      Collection<Listener> values = getListeners(path);
      if (values.remove(listener) && values.isEmpty()) {
        unmonitor(path);
      }
    }
  }

  @Override public boolean isWatchable(Path path) {
    LocalPath.check(path);
    for (Path unwatchable : ignored) {
      if (path.startsWith(unwatchable)) {
        return false;
      }
    }
    return true;
  }

  @Override public void close() {
    synchronized (this) {
      for (PathObserver observer : observers) {
        observer.stopWatching();
      }
      observers.clear();
      monitored.clear();
      listeners.clear();
    }
  }

  @Override public boolean isRegistered(Path path) {
    synchronized (this) {
      return monitored.contains(path);
    }
  }

  boolean hasObserver(Path path) {
    synchronized (this) {
      for (PathObserver observer : observers) {
        if (observer.hasPath(path)) {
          return true;
        }
      }
      return false;
    }
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
  private void checkNode(Path path, Node node) {
    for (PathObserver observer : observers) {
      if (observer.hasPath(path)) {
        if (!observer.getNode().equals(node)) {
          observer.stopWatching();
          removeSubtree(observer);
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
  private PathObserver checkObserver(Node node) {
    PathObserver observer = findObserver(node);
    if (observer == null) {
      return null;
    }

    List<Path> removed = observer.removeNonExistPaths();
    monitored.removeAll(removed);

    if (stopAndRemoveIfNoPath(observer, observers)) {
      observer = null;
    }

    for (Path p : removed) {
      removeChildMonitors(p);
      removeChildObservers(p);
    }

    return observer;
  }

  private PathObserver findObserver(Node node) {
    for (PathObserver observer : observers) {
      if (observer.getNode().equals(node)) {
        return observer;
      }
    }
    return null;
  }

  private void monitor(Path path) throws ErrnoException {
    if (!isWatchable(path)) {
      return;
    }

    LocalFileStatus status = LocalFileStatus.stat(path, false);
    Node node = Node.from(status);

    synchronized (this) {
      checkNode(path, node);
      if (!monitored.add(path)) {
        return;
      }
      startObserver(path, node);
    }

    startObserversForSubDirs(status);
  }

  private void unmonitor(Path parent) {
    synchronized (this) {
      if (!monitored.remove(parent)) {
        return;
      }

      listeners.remove(parent);

      Iterator<PathObserver> it = observers.iterator();
      while (it.hasNext()) {
        PathObserver observer = it.next();
        observer.removePath(parent);
        for (Path path : observer.copyPaths()) {
          if (parent.equals(path.parent()) && !monitored.contains(path)) {
            observer.removePath(path);
          }
        }
        stopAndRemoveIfNoPath(observer, it);
      }
    }
  }

  private void startObserversForSubDirs(LocalFileStatus parent) {
    // Using a directory stream without stating the children will be faster
    // especially on large directories
    try (LocalDirectoryStream stream = LocalDirectoryStream.open(parent.path())) {
      for (LocalDirectoryEntry entry : stream.local()) {
        if (entry.isDirectory()) {
          Path child = entry.path();
          if (!isWatchable(child)) {
            continue;
          }
          try {
            startObserver(child, Node.create(parent.device(), entry.ino()));
          } catch (FileSystemException e) {
            // File no longer exits or inaccessible, ignore;
          }
        }
      }
    }
  }

  private void startObserver(Path path, Node node) {
    synchronized (this) {
      checkNode(path, node);
      PathObserver observer = checkObserver(node);
      if (observer == null) {
        observer = new PathObserver(path, node, MODIFICATION_MASK, processor);
        observer.startWatching();
        observers.add(observer);
      }
      observer.addPath(path);
    }
  }

  private void onObserverStopped(PathObserver observer) {
    logger.debug("Stopped observer %s", observer);
    removeSubtree(observer);
  }

  /**
   * When an observer is stopped due to the path being deleted/moved/invalid,
   * call this to stop observers that are monitoring on child paths, as they are
   * now no longer valid after the parent path being deleted/moved.
   */
  private void removeSubtree(PathObserver observer) {
    synchronized (this) {
      List<Path> removed = observer.removePaths();
      monitored.removeAll(removed);
      observers.remove(observer);
      for (Path path : removed) {
        removeChildMonitors(path);
        removeChildObservers(path);
      }
    }
  }

  private void removeChildMonitors(Path parent) {
    Iterator<Path> it = monitored.iterator();
    while (it.hasNext()) {
      Path path = it.next();
      if (path.startsWith(parent)) {
        it.remove();
      }
    }
  }

  private void removeChildObservers(Path parent) {
    Iterator<PathObserver> it = observers.iterator();
    while (it.hasNext()) {
      PathObserver observer = it.next();
      List<Path> removed = observer.removeChildPaths(parent);
      monitored.removeAll(removed);
      stopAndRemoveIfNoPath(observer, it);
    }
  }

  private void onCreate(Path path) {
    try {
      LocalFileStatus file = LocalFileStatus.stat(path, false);
      if (file.isDirectory() && isRegistered(path.parent())) {
        startObserver(path, Node.from(file));
      }
    } catch (FileSystemException e) {
      // Path no longer exists, permission etc, ignore and continue
      logger.warn(e, "Failed to stat %s", path);
    }
  }

  private void onDelete(Path path) {
    boolean accessible = true;
    try {
      checkExist(path.toString());
    } catch (ErrnoException e) {
      accessible = false;
    }
    if (!accessible) {
      synchronized (this) {
        removePath(path);
      }
    }
  }

  private void removePath(Path path) {
    monitored.remove(path);
    removeChildMonitors(path);
    removeChildObservers(path);
    Iterator<PathObserver> it = observers.iterator();
    while (it.hasNext()) {
      PathObserver observer = it.next();
      if (observer.removePath(path)) {
        stopAndRemoveIfNoPath(observer, it);
        break;
      }
    }
  }

  private boolean stopAndRemoveIfNoPath(
      PathObserver observer, Collection<? extends PathObserver> observers) {

    if (observer.getPathCount() == 0) {
      observer.stopWatching();
      observers.remove(observer);
      return true;
    }
    return false;
  }

  private boolean stopAndRemoveIfNoPath(
      PathObserver observer, Iterator<PathObserver> it) {

    if (observer.getPathCount() == 0) {
      observer.stopWatching();
      it.remove();
      return true;
    }
    return false;
  }

  @Override public String toString() {
    synchronized (this) {
      return toStringHelper(this)
          .add("monitors", monitored)
          .add("observers", observers)
          .add("listeners", listeners)
          .toString();
    }
  }
}
