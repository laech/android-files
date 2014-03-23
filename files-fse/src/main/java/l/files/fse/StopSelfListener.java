package l.files.fse;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A listener that will stop the file observer when the directory is deleted or
 * moved.
 */
final class StopSelfListener extends EventAdapter {

  private final EventObserver observer;
  private final Callback callback;

  StopSelfListener(EventObserver observer, Callback callback) {
    this.observer = checkNotNull(observer, "observer");
    this.callback = checkNotNull(callback, "callback");
  }

  @Override public void onDeleteSelf(String path) {
    super.onDeleteSelf(path);
    stop();
  }

  @Override public void onMoveSelf(String path) {
    super.onMoveSelf(path);

    // TODO better handle this, check the inode
    /*
     * Sometimes when a directory is moved from else where, a MOVE_TO is
     * notified on the monitored parent, but *sometimes* a MOVE_SELF is notified
     * after monitoring on the newly added file starts, so this is a temporary
     * fix for that. This directory could also exists if the original is moved
     * somewhere else and a new one is quickly added in place, then this code
     * will be wrong.
     */
    if (!observer.getDirectory().exists()) {
      stop();
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
