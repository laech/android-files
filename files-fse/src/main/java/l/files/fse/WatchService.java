package l.files.fse;

import l.files.io.file.Path;

/**
 * Service designed for monitoring file changes within a directory.
 * <p/>
 * When {@link #register(Path, WatchEvent.Listener)} on a directory, all
 * files/directories of the given directory will be monitored for changes
 * (non-recursive), including file addition/modification/deletion and anything
 * that changes the attribute of the files/directories. This is non-recursive,
 * meaning only changes to files/directories at the current depth level will be
 * reported.
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
   * path will be sent to the given registered listeners.
   *
   * @throws WatchException if failed to monitor the given file
   */
  public abstract void register(Path path, WatchEvent.Listener listener);

  /**
   * Stops monitoring on the given file path.
   */
  public abstract void unregister(Path path, WatchEvent.Listener listener);

  /**
   * Checks whether the given file is currently being monitored.
   */
  abstract boolean isMonitored(Path path);

  /**
   * Shuts down this instance. This will stop all observers. Intended for
   * testing.
   */
  abstract void stopAll();

  /**
   * Checks whether there is a running observer at the given location. Intended
   * for testing.
   */
  abstract boolean hasObserver(Path path);
}
