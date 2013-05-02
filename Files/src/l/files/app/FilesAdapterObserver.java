package l.files.app;

import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import l.files.widget.UpdatableAdapter;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayList;
import static l.files.BuildConfig.DEBUG;
import static l.files.util.FileSort.BY_NAME;

public class FilesAdapterObserver extends FileObserver {

  private static final String TAG = FilesAdapterObserver.class.getSimpleName();

  /**
   * Wait for this amount of milliseconds when an event is received before
   * updating the adapter, batching subsequent events then update in one go,
   * this is more efficient and avoids interrupting any animations for the
   * previous update.
   */
  private static final long BATCH_UPDATE_DELAY = 50;
  private static final int MASK = CREATE | DELETE | MOVED_FROM | MOVED_TO;

  private final List<File> removals = newArrayList();
  private final List<File> additions = newArrayList();

  private final Runnable handleRemovals = new Runnable() {
    @Override public void run() {
      adapter.removeAll(purge(removals));
    }
  };

  private final Runnable handleAdditions = new Runnable() {
    @Override public void run() {
      adapter.addAll(purge(additions), BY_NAME);
    }
  };

  private final UpdatableAdapter<File> adapter;
  private final Handler handler;
  private final File dir;

  public FilesAdapterObserver(
      File dir, UpdatableAdapter<File> adapter, Handler handler) {
    super(checkNotNull(dir, "dir").getAbsolutePath(), MASK);
    this.dir = dir;
    this.adapter = checkNotNull(adapter, "adapter");
    this.handler = checkNotNull(handler, "handler");
  }

  @Override public void onEvent(int event, final String path) {
    if (DEBUG) log(event, path);

    if (fileDisappeared(event))
      update(removals, handleRemovals, path);

    else if (fileAppeared(event))
      update(additions, handleAdditions, path);
  }

  private Collection<File> purge(Collection<File> collection) {
    synchronized (this) {
      List<File> snapshot = newArrayList(collection);
      collection.clear();
      return snapshot;
    }
  }

  private void update(Collection<File> files, Runnable update, String path) {
    synchronized (this) {
      files.add(new File(dir, path));
      handler.removeCallbacks(update);
      handler.postDelayed(update, BATCH_UPDATE_DELAY);
    }
  }

  private boolean fileAppeared(int event) {
    return (event & CREATE) != 0 || (event & MOVED_TO) != 0;
  }

  private boolean fileDisappeared(int event) {
    return (event & DELETE) != 0 || (event & MOVED_FROM) != 0;
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
    Log.d(TAG, event + ", dir=" + dir + ", lastModified=" + dir.lastModified() + ", path=" + path);
  }
}
