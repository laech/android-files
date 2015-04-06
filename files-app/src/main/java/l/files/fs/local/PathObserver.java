package l.files.fs.local;

import android.os.Handler;
import android.os.Message;

import com.google.common.base.Predicate;
import com.google.common.base.Throwables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import l.files.fs.Path;
import l.files.fs.local.android.os.FileObserver;
import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static com.google.common.base.MoreObjects.toStringHelper;

final class PathObserver extends FileObserver {

  private static final Logger logger = Logger.get(PathObserver.class);

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
      throw Throwables.propagate((Throwable) msg.obj);
    }
  };

  private final Path path;
  private final Node node;
  private final Listener listener;
  private final Set<Path> paths;

  public PathObserver(Path path, Node node, int mask, Listener listener) {
    super(path.toString(), mask);
    this.path = path;
    this.node = node;
    this.listener = listener;
    this.paths = new HashSet<>();
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

  public Collection<Path> copyPaths() {
    synchronized (this) {
      return new ArrayList<>(paths);
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
      List<Path> result = new ArrayList<>(paths.size());
      for (Path path : paths) {
        if (pred.apply(path)) {
          result.add(path);
        }
      }
      paths.removeAll(result);
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
      @Override public boolean apply(Path path) {
        return !path.getResource().exists();
      }
    });
  }

  public List<Path> removePaths() {
    synchronized (this) {
      List<Path> result = new ArrayList<>(paths);
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

      if (0 != (event & MOVE_SELF)) {
        checkNode();
      }
      forward(event, path);

    } catch (Throwable e) {
      rethrow(e);
    }
  }

  private void forward(int event, String child) {
    log(event, child);
    listener.onEvent(this, event, child);
  }

  private void rethrow(Throwable e) {
    Message.obtain(rethrow, 0, e).sendToTarget();
    stopWatching();
  }

  /*
   * Sometimes when a directory is moved from else where, a MOVE_TO is
   * notified on the monitored parent, but *sometimes* a MOVE_SELF is notified
   * after monitoring on the newly added file starts, so this is a temporary
   * fix for that. This directory could also exists if the original is moved
   * somewhere else and a new one is quickly added in place, then this code
   * will be wrong.
   */
  private void checkNode() {
    try {

      LocalResourceStatus file = LocalResourceStatus.stat(path, false);
      if (!Node.from(file).equals(node)) {
        stopWatching();
      }

    } catch (IOException e) {
      stopWatching();
    }
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
    logger.verbose("%s, parent=%s, path=%s", getEventName(event), path, child);
  }

  @Override public String toString() {
    synchronized (this) {
      return toStringHelper(this)
          .add("path", path)
          .add("paths", paths)
          .toString();
    }
  }

  static interface Listener {
    void onEvent(PathObserver observer, int event, String child);
  }
}
