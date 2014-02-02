package l.files.provider;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;

import static l.files.common.io.Files.normalize;

public class DirWatcher extends FileObserver {

  private static final String TAG = DirWatcher.class.getSimpleName();

  private final File dir;
  private volatile DirWatcherListener[] listeners;

  public DirWatcher(File dir, int mask) {
    super(dir.getAbsolutePath(), mask);
    this.dir = normalize(dir);
  }

  public File getDirectory() {
    return dir;
  }

  @Override public void onEvent(int event, final String path) {
//    if (DEBUG) {
//      log(event, path);
//    }
    if ((event & OPEN) != 0) onOpen(path);
    else if ((event & ACCESS) != 0) onAccess(path);
    else if ((event & ATTRIB) != 0) onAttrib(path);
    else if ((event & CREATE) != 0) onCreate(path);
    else if ((event & DELETE) != 0) onDelete(path);
    else if ((event & MODIFY) != 0) onModify(path);
    else if ((event & MOVED_TO) != 0) onMovedTo(path);
    else if ((event & MOVE_SELF) != 0) onMoveSelf(path);
    else if ((event & MOVED_FROM) != 0) onMovedFrom(path);
    else if ((event & CLOSE_WRITE) != 0) onCloseWrite(path);
    else if ((event & DELETE_SELF) != 0) onDeleteSelf(path);
    else if ((event & CLOSE_NOWRITE) != 0) onCloseNoWrite(path);
  }

  private void onCloseNoWrite(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onCloseNoWrite(path);
    }
  }

  private void onDeleteSelf(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onDeleteSelf(path);
    }
  }

  private void onCloseWrite(String path) {
    for (DirWatcherListener listener : listeners)
      listener.onCloseWrite(path);
  }

  private void onMovedFrom(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onMovedFrom(path);
    }
  }

  private void onMoveSelf(String path) {
    for (DirWatcherListener listener : listeners)
      listener.onMoveSelf(path);
  }

  private void onMovedTo(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onMovedTo(path);
    }
  }

  private void onModify(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onModify(path);
    }
  }

  private void onDelete(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onDelete(path);
    }
  }

  private void onCreate(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onCreate(path);
    }
  }

  private void onAttrib(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onAttrib(path);
    }
  }

  private void onAccess(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onAccess(path);
    }
  }

  private void onOpen(String path) {
    for (DirWatcherListener listener : listeners) {
      listener.onOpen(path);
    }
  }

  public void setListeners(DirWatcherListener... listeners) {
    this.listeners = listeners;
  }

  private void log(int event, String path) {
    if ((event & OPEN) != 0) debug("OPEN", path);
    else if ((event & ACCESS) != 0) debug("ACCESS", path);
    else if ((event & ATTRIB) != 0) debug("ATTRIB", path);
    else if ((event & CREATE) != 0) debug("CREATE", path);
    else if ((event & DELETE) != 0) debug("DELETE", path);
    else if ((event & MODIFY) != 0) debug("MODIFY", path);
    else if ((event & MOVED_TO) != 0) debug("MOVED_TO", path);
    else if ((event & MOVE_SELF) != 0) debug("MOVE_SELF", path);
    else if ((event & MOVED_FROM) != 0) debug("MOVED_FROM", path);
    else if ((event & CLOSE_WRITE) != 0) debug("CLOSE_WRITE", path);
    else if ((event & DELETE_SELF) != 0) debug("DELETE_SELF", path);
    else if ((event & CLOSE_NOWRITE) != 0) debug("CLOSE_NOWRITE", path);
    else debug("UNKNOWN", path);
  }

  private void debug(String event, String path) {
    Log.d(TAG, event + ", parent=" + dir + ", path=" + path);
  }
}
