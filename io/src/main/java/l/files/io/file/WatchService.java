package l.files.io.file;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;

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

  /**
   * System directories such as /dev, /proc contain special files (aren't really
   * files), they generate tons of file system events (MODIFY, CLOSE_WRITE...)
   * and they don't change. WatchService should not allow them and their sub
   * paths to be watched.
   */
  static final ImmutableSet<Path> IGNORED = ImmutableSet.of(
      Path.from("/proc"),
      Path.from("/dev")
  );

  private static final WatchService INSTANCE = new WatchServiceImpl(IGNORED);

  public static WatchService get() {
    return INSTANCE;
  }

  /**
   * Starts monitoring on the given file path, file systems event on the given
   * path will be sent to the given registered listeners.
   */
  public abstract void register(Path path, WatchEvent.Listener listener) throws IOException;

  /**
   * Stops monitoring on the given file path.
   */
  public abstract void unregister(Path path, WatchEvent.Listener listener);

  /**
   * Returns true if the path can be watched, false otherwise.
   */
  public abstract boolean isIgnored(Path path);

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
