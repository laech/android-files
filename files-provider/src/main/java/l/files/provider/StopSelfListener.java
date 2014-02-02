package l.files.provider;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A listener that will stop the file observer when the directory is deleted or
 * moved.
 */
final class StopSelfListener extends DirWatcherListenerAdapter {

  private final DirWatcher observer;
  private final Callback callback;

  StopSelfListener(DirWatcher observer, Callback callback) {
    this.observer = checkNotNull(observer, "observer");
    this.callback = checkNotNull(callback, "callback");
  }

  @Override public void onDeleteSelf(String path) {
    super.onDeleteSelf(path);
    stop();
  }

  @Override public void onMoveSelf(String path) {
    super.onMoveSelf(path);
    stop();
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
    void onObserverStopped(DirWatcher observer);
  }
}
