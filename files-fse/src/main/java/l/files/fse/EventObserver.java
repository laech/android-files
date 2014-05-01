package l.files.fse;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.io.Path;
import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Sets.newHashSet;

final class EventObserver extends FileObserver {

  private static final Logger logger = Logger.get(EventObserver.class);

  // Extra inotify constants not defined in FileObserver
  public static final int IN_UNMOUNT = 0x00002000;
  public static final int IN_Q_OVERFLOW = 0x00004000;
  public static final int IN_IGNORED = 0x00008000;

  /**
   * FileObserver will catch any throwable and ignore it, not good as we won't
   * be notified of any coding errors. Instead we catch the exception and send
   * it off to the main thread and rethrow it there, this will cause the
   * application to crash and we can fix the root cause.
   */
  private static final Handler rethrow = new Handler(getMainLooper()) {
    @Override public void handleMessage(Message msg) {
      super.handleMessage(msg);
      throw (RuntimeException) msg.obj;
    }
  };

  private final Path path;
  private final Node node;
  private final Set<EventListener> listeners;
  private final Set<Path> paths;

  public EventObserver(Path path, Node node, int mask) {
    super(path.toString(), mask);
    this.path = path;
    this.node = node;
    this.listeners = new CopyOnWriteArraySet<>();
    this.paths = newHashSet();
    addPath(path);
  }

  @Override public void startWatching() {
    super.startWatching();
    logger.debug("Start %s", this);
  }

  @Override public void stopWatching() {
    super.stopWatching();
    logger.debug("Stop %s", this);
  }

  public void addListener(EventListener listener) {
    this.listeners.add(listener);
  }

  public Collection<Path> copyPaths() {
    synchronized (this) {
      return newArrayList(paths);
    }
  }

  /**
   * Adds a path to this observer, the given path must have the same inode as
   * {@link #getNode()}. This observer does not use the given path, this method
   * exists simply as a utility for callers to keep track of paths that are
   * pointed to the same inode. Does nothing if the path has already been
   * added.
   *
   * @return true if path is added, false if already exists
   */
  public boolean addPath(Path path) {
    synchronized (this) {
      return paths.add(path);
    }
  }

  /**
   * Removes the given path from this observer.
   *
   * @return true if path is removed, false if path does not exists
   */
  public boolean removePath(Path path) {
    synchronized (this) {
      return paths.remove(path);
    }
  }

  /**
   * Remove and returns all the paths that passed the given predicate.
   */
  public List<Path> removePaths(Predicate<Path> pred) {
    synchronized (this) {
      List<Path> result = newArrayListWithCapacity(paths.size());
      Iterator<Path> it = paths.iterator();
      while (it.hasNext()) {
        Path path = it.next();
        if (pred.apply(path)) {
          it.remove();
          result.add(path);
        }
      }
      return result;
    }
  }

  List<Path> removeChildPaths(final Path parent) {
    return removePaths(new Predicate<Path>() {
      @Override public boolean apply(Path input) {
        return input.startsWith(parent);
      }
    });
  }

  public List<Path> removeNonExistPaths() {
    return removePaths(new Predicate<Path>() {
      @Override public boolean apply(Path input) {
        return !input.toFile().exists();
      }
    });
  }

  public List<Path> removePaths() {
    synchronized (this) {
      List<Path> result = newArrayList(paths);
      paths.clear();
      return result;
    }
  }

  public int getPathCount() {
    synchronized (this) {
      return paths.size();
    }
  }

  public boolean hasPath(Path path) {
    synchronized (this) {
      return paths.contains(path);
    }
  }

  public Node getNode() {
    return node;
  }

  @Override public void onEvent(int event, final String path) {
    try {
      forward(event, path);
    } catch (Throwable e) {
      rethrow(e);
    }
  }

  private void forward(int event, String path) {
    log(event, path);
    for (EventListener listener : listeners) {
      listener.onEvent(event, path);
    }
  }

  private void rethrow(Throwable e) {
    Message.obtain(rethrow, 0, e).sendToTarget();
    stopWatching();
  }

  static String getEventName(int event) {
    if (0 != (event & OPEN)) return "OPEN";
    if (0 != (event & ACCESS)) return "ACCESS";
    if (0 != (event & ATTRIB)) return "ATTRIB";
    if (0 != (event & CREATE)) return "CREATE";
    if (0 != (event & DELETE)) return "DELETE";
    if (0 != (event & MODIFY)) return "MODIFY";
    if (0 != (event & MOVED_TO)) return "MOVED_TO";
    if (0 != (event & MOVE_SELF)) return "MOVE_SELF";
    if (0 != (event & MOVED_FROM)) return "MOVED_FROM";
    if (0 != (event & CLOSE_WRITE)) return "CLOSE_WRITE";
    if (0 != (event & DELETE_SELF)) return "DELETE_SELF";
    if (0 != (event & CLOSE_NOWRITE)) return "CLOSE_NOWRITE";
    if (0 != (event & IN_IGNORED)) return "IN_IGNORED";
    if (0 != (event & IN_Q_OVERFLOW)) return "IN_Q_OVERFLOW";
    if (0 != (event & IN_UNMOUNT)) return "IN_UNMOUNT";
    return "UNKNOWN";
  }

  private void log(int event, String child) {
    logger.debug("%s, parent=%s, path=%s", getEventName(event), path, child);
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("path", path)
        .add("paths", paths)
        .toString();
  }
}
