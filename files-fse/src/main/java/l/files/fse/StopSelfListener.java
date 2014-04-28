package l.files.fse;

import l.files.io.Path;
import l.files.logging.Logger;
import l.files.os.ErrnoException;
import l.files.os.Stat;

import static android.os.FileObserver.DELETE_SELF;
import static android.os.FileObserver.MOVE_SELF;
import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.os.Stat.stat;

/**
 * A listener that will stop the file observer when the directory is deleted or
 * moved.
 */
final class StopSelfListener implements EventListener {

  private static final Logger logger = Logger.get(StopSelfListener.class);

  private final EventObserver observer;
  private final Callback callback;
  private final Node node;
  private final Path path;

  /**
   * @param observer the observer this listener is registered to
   * @param callback the callback to be notified when observer is stopped
   * @param node the node of the currently monitored file path
   * @param path the path of the currently monitored file
   */
  StopSelfListener(
      EventObserver observer, Callback callback, Node node, Path path) {
    this.observer = checkNotNull(observer, "observer");
    this.callback = checkNotNull(callback, "callback");
    this.node = checkNotNull(node, "node");
    this.path = checkNotNull(path, "path");
  }

  @Override public void onEvent(int event, String path) {

    if (isSelfDeleted(event)) {
      stop();

    } else if (isSelfMoved(event)) {
      checkNode();
    }
  }

  private boolean isSelfMoved(int event) {
    return 0 != (event & MOVE_SELF);
  }

  private boolean isSelfDeleted(int event) {
    return 0 != (event & DELETE_SELF);
  }

  private void stop() {
    observer.stopWatching();
    callback.onObserverStopped(observer);
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

      Stat stat = stat(this.path.toString());
      if (!Node.from(stat).equals(node)) {
        stop();
      }

    } catch (ErrnoException e) {
      stop();
      logger.info(e, "Stopping observer on exception %s", observer);
    }
  }

  static interface Callback {
    /**
     * Called when the given observer is stopped. This will be called on the
     * same thread as the observer's event thread.
     */
    void onObserverStopped(EventObserver observer);
  }
}
