package l.files.fse;

import l.files.common.logging.Logger;
import l.files.os.OsException;
import l.files.os.Stat;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A listener that will stop the file observer when the directory is deleted or
 * moved.
 */
final class StopSelfListener extends EventAdapter {

  private static final Logger logger = Logger.get(StopSelfListener.class);

  private final EventObserver observer;
  private final Callback callback;
  private final long inode;

  StopSelfListener(EventObserver observer, Callback callback, long inode) {
    this.observer = checkNotNull(observer, "observer");
    this.callback = checkNotNull(callback, "callback");
    this.inode = inode;
  }

  @Override public void onDeleteSelf(String path) {
    super.onDeleteSelf(path);
    stop();
  }

  @Override public void onMoveSelf(String path) {
    super.onMoveSelf(path);

    /*
     * Sometimes when a directory is moved from else where, a MOVE_TO is
     * notified on the monitored parent, but *sometimes* a MOVE_SELF is notified
     * after monitoring on the newly added file starts, so this is a temporary
     * fix for that. This directory could also exists if the original is moved
     * somewhere else and a new one is quickly added in place, then this code
     * will be wrong.
     */
    try {
      if (Stat.stat(observer.getPath()).ino != inode) {
        stop();
      }
    } catch (OsException e) {
      stop();
      logger.info(e, "Stopping observer on exception %s", observer.getPath());
    }
  }

  private void stop() {
    observer.stopWatching();
    callback.onObserverStopped(observer);
  }

  static interface Callback {
    /**
     * Called when the given observer is stopped. This will be called on the
     * same thread as the observer's event thread.
     */
    void onObserverStopped(EventObserver observer);
  }
}
