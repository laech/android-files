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
      handleEvent(event, path);
    } catch (Throwable e) {
      Message.obtain(rethrow, 0, e).sendToTarget();
      stopWatching();
    }
  }

  private void handleEvent(int event, String path) {
    log(event, path);

    if ((event & OPEN) != 0) onOpen(path);
    if ((event & ACCESS) != 0) onAccess(path);
    if ((event & ATTRIB) != 0) onAttrib(path);
    if ((event & CREATE) != 0) onCreate(path);
    if ((event & DELETE) != 0) onDelete(path);
    if ((event & MODIFY) != 0) onModify(path);
    if ((event & MOVED_TO) != 0) onMovedTo(path);
    if ((event & MOVE_SELF) != 0) onMoveSelf(path);
    if ((event & MOVED_FROM) != 0) onMovedFrom(path);
    if ((event & CLOSE_WRITE) != 0) onCloseWrite(path);
    if ((event & DELETE_SELF) != 0) onDeleteSelf(path);
    if ((event & CLOSE_NOWRITE) != 0) onCloseNoWrite(path);
  }

  private void onCloseNoWrite(String path) {
    for (EventListener listener : listeners) {
      listener.onCloseNoWrite(path);
    }
  }

  private void onDeleteSelf(String path) {
    for (EventListener listener : listeners) {
      listener.onDeleteSelf(path);
    }
  }

  private void onCloseWrite(String path) {
    for (EventListener listener : listeners)
      listener.onCloseWrite(path);
  }

  private void onMovedFrom(String path) {
    for (EventListener listener : listeners) {
      listener.onMovedFrom(path);
    }
  }

  private void onMoveSelf(String path) {
    for (EventListener listener : listeners)
      listener.onMoveSelf(path);
  }

  private void onMovedTo(String path) {
    for (EventListener listener : listeners) {
      listener.onMovedTo(path);
    }
  }

  private void onModify(String path) {
    for (EventListener listener : listeners) {
      listener.onModify(path);
    }
  }

  private void onDelete(String path) {
    for (EventListener listener : listeners) {
      listener.onDelete(path);
    }
  }

  private void onCreate(String path) {
    for (EventListener listener : listeners) {
      listener.onCreate(path);
    }
  }

  private void onAttrib(String path) {
    for (EventListener listener : listeners) {
      listener.onAttrib(path);
    }
  }

  private void onAccess(String path) {
    for (EventListener listener : listeners) {
      listener.onAccess(path);
    }
  }

  private void onOpen(String path) {
    for (EventListener listener : listeners) {
      listener.onOpen(path);
    }
  }

  private void log(int event, String path) {
    if ((event & OPEN) != 0) debug("OPEN", path);
    if ((event & ACCESS) != 0) debug("ACCESS", path);
    if ((event & ATTRIB) != 0) debug("ATTRIB", path);
    if ((event & CREATE) != 0) debug("CREATE", path);
    if ((event & DELETE) != 0) debug("DELETE", path);
    if ((event & MODIFY) != 0) debug("MODIFY", path);
    if ((event & MOVED_TO) != 0) debug("MOVED_TO", path);
    if ((event & MOVE_SELF) != 0) debug("MOVE_SELF", path);
    if ((event & MOVED_FROM) != 0) debug("MOVED_FROM", path);
    if ((event & CLOSE_WRITE) != 0) debug("CLOSE_WRITE", path);
    if ((event & DELETE_SELF) != 0) debug("DELETE_SELF", path);
    if ((event & CLOSE_NOWRITE) != 0) debug("CLOSE_NOWRITE", path);
  }

  private void debug(String event, String child) {
    logger.debug("%s, parent=%s, path=%s", event, paths, child);
  }

  @Override public String toString() {
    return Objects.toStringHelper(this)
        .add("path", path)
        .add("paths", paths)
        .toString();
  }
}
