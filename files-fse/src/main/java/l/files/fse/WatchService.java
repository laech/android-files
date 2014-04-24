package l.files.fse;

import l.files.io.Path;

/**
 * Service designed for monitoring file changes within a directory.
 * <p/>
 * When {@link #monitor(Path)} on a directory, all files/directories of the
 * given directory will be monitored for changes (non-recursive), including file
 * addition/modification/deletion and anything that changes the attribute of the
 * files/directories. This is non-recursive, meaning only changes to
 * files/directories at the current depth level will be reported.
 * <p/>
 * Note that by the time a listener is notified, the target file may have
 * already be changed, therefore a robust application should have an alternative
 * way of handling instead of reply on this fully.
 */
public abstract class WatchService {

  /*
    Note:
    Two FileObserver instances cannot be monitoring on the same inode, if one is
    stopped, the other will also be stopped, because FileObserver internally
    uses global states.
   */

  WatchService() {}

  private static final WatchService INSTANCE = new WatchServiceImpl();

  public static WatchService get() {
    return INSTANCE;
  }

  /**
   * Starts monitoring on the given file path, file systems event on the given
   * path will be sent to all registered listeners.
   *
   * @throws WatchException if failed to monitor the given file
   */
  public abstract void monitor(Path path);

  /**
   * Stops monitoring on the given file path.
   */
  public abstract void unmonitor(Path path);

  /**
   * Checks whether the given file is currently being monitored.
   */
  public abstract boolean isMonitored(Path path);

  /**
   * Registers a listener to be notified of events. Has no affect if the
   * listener is already registered.
   */
  public abstract void register(WatchEvent.Listener listener);

  /**
   * Unregisters a listener. Has no affect if the listener is not registered.
   */
  public abstract void unregister(WatchEvent.Listener listener);

  /**
   * Shuts down this instance. This will stop all observers. Intended for
   * testing.
   */
  public abstract void stopAll();

  /**
   * Checks whether there is a running observer at the given location. Intended
   * for testing.
   */
  public abstract boolean hasObserver(Path path);
}
