package l.files.ui.app.files;

import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.BuildConfig.DEBUG;

public class DirectoryObserver extends FileObserver {

  private static final String TAG = DirectoryObserver.class.getSimpleName();

  /**
   * Wait for this amount of milliseconds when an event is received before
   * notifying the listener, batching subsequent events then update in one go,
   * this is more efficient and avoids interrupting any animations for the
   * previous update.
   */
  static final long BATCH_UPDATE_DELAY = 50;

  static final int DIR_CHANGED_MASK = CREATE | DELETE | MOVED_FROM | MOVED_TO;

  private final File dir;
  private final Handler handler;
  private final Runnable listener;

  public DirectoryObserver(File dir, Handler handler, Runnable listener) {
    super(checkNotNull(dir, "dir").getAbsolutePath(), DIR_CHANGED_MASK);
    this.dir = dir;
    this.handler = checkNotNull(handler, "handler");
    this.listener = checkNotNull(listener, "listener");
  }

  @Override public void onEvent(int event, final String path) {
    if (DEBUG) log(event, path);

    if ((event & DIR_CHANGED_MASK) != 0) {
      handler.removeCallbacks(listener);
      handler.postDelayed(listener, BATCH_UPDATE_DELAY);
    }
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
    Log.d(TAG, event +
        ", dir=" + dir +
        ", lastModified=" + dir.lastModified() +
        ", path=" + path);
  }
}
