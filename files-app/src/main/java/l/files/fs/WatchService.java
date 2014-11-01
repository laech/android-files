package l.files.fs;

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
public interface WatchService extends AutoCloseable {

  /**
   * Starts monitoring on the given path, file systems event on the given
   * path will be sent to the given registered listeners.
   *
   * @throws FileSystemException      if failed to listener on the given path
   * @throws IllegalArgumentException if path is not of this file system
   */
  void register(Path path, WatchEvent.Listener listener);

  /**
   * Stops monitoring on the given path.
   *
   * @throws IllegalArgumentException if path is not of this file system
   */
  void unregister(Path path, WatchEvent.Listener listener);

  /**
   * Returns true if the path can be watched, false otherwise.
   *
   * @throws IllegalArgumentException if path is not of this file system
   */
  boolean isWatchable(Path path);

  /**
   * @throws FileSystemException if failed to close this service
   */
  @Override void close();

}
