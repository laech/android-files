package l.files.fse;

import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

final class EventObserver extends FileObserver {

  private static final Logger logger = Logger.get(EventObserver.class);

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

  private final String path;
  private final Node node;
  private final Set<EventListener> listeners;
  private final Set<String> paths;

  public EventObserver(String path, Node node, int mask) {
    super(path, mask);
    this.path = path;
    this.node = node;
    this.listeners = new CopyOnWriteArraySet<>();
    this.paths = newHashSet();
    addPath(path);
  }

  public void addListener(EventListener listener) {
    this.listeners.add(listener);
  }

  public Collection<String> copyPaths() {
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
  public boolean addPath(String path) {
    synchronized (this) {
      return paths.add(path);
    }
  }

  /**
   * Removes the given path from this observer.
   *
   * @return true if path is removed, false if path does not exists
   */
  public boolean removePath(String path) {
    synchronized (this) {
      return paths.remove(path);
    }
  }

  /**
   * Remove and returns all the paths that passed the given predicate.
   */
  public List<String> removePaths(Predicate<String> pred) {
    synchronized (this) {
      List<String> result = Lists.newArrayListWithCapacity(paths.size());
      Iterator<String> it = paths.iterator();
      while (it.hasNext()) {
        String path = it.next();
        if (pred.apply(path)) {
          it.remove();
          result.add(path);
        }
      }
      return result;
    }
  }

  List<String> removeChildPaths(String parent) {
    final String prefix = parent + "/";
    return removePaths(new Predicate<String>() {
      @Override public boolean apply(String input) {
        return input.startsWith(prefix);
      }
    });
  }

  public List<String> removeNonExistPaths() {
    return removePaths(new Predicate<String>() {
      @Override public boolean apply(String input) {
        return !new File(input).exists();
      }
    });
  }

  public List<String> removePaths() {
    synchronized (this) {
      List<String> result = newArrayList(paths);
      paths.clear();
      return result;
    }
  }

  public int getPathCount() {
    synchronized (this) {
      return paths.size();
    }
  }

  public boolean hasPath(String path) {
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
    return "UNKNOWN";
  }

  private void log(int event, String child) {
    logger.debug("%s, parent=%s, path=%s", getEventName(event), paths, child);
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("path", path)
        .add("paths", paths)
        .toString();
  }
}
