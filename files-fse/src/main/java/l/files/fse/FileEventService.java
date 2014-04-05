package l.files.fse;

import com.google.common.base.Optional;

import java.io.File;
import java.util.List;

/**
 * Service designed for monitoring file changes within a directory.
 * <p/>
 * When {@link #monitor2(File)} on a directory, all files/directories of the
 * given directory will be monitored for changes (non-recursive), including file
 * addition/modification/deletion and anything that changes the attribute of the
 * files/directories. This is non-recursive, meaning only changes to
 * files/directories at the current depth level will be reported.
 * <p/>
 * Note that by the time a listener is notified, the target file may have
 * already be changed, therefore a robust application should have an alternative
 * way of handling instead of reply on this fully.
 */
public abstract class FileEventService {

  /*
    Note:
    Two FileObserver instances cannot be monitoring on the same inode, if one is
    stopped, the other will also be stopped, because FileObserver internally
    uses global states.
   */

  FileEventService() {}

  private static final FileEventService INSTANCE = new FileEventServiceImpl();

  public static FileEventService get() {
    return INSTANCE;
  }

  /**
   * Starts monitoring on the given file path, file systems event on the given
   * path will be sent to all registered listeners.
   *
   * @return if this is the first time the path is being monitored, returns the
   * children information (if any) successfully retrieved
   * @throws EventException if failed to monitor the given file
   */
  public abstract Optional<List<PathStat>> monitor2(File file);

  /**
   * Stops monitoring on the given file path.
   */
  public abstract void unmonitor(File file);

  /**
   * Checks whether the given file is currently being monitored.
   */
  public abstract boolean isMonitored(File file);

  /**
   * Registers a listener to be notified of events. Has no affect if the
   * listener is already registered.
   */
  public abstract void register(FileEventListener listener);

  /**
   * Unregisters a listener. Has no affect if the listener is not registered.
   */
  public abstract void unregister(FileEventListener listener);

  /**
   * Shuts down this instance. This will stop all observers. Intended for
   * testing.
   */
  public abstract void stopAll();

  /**
   * Checks whether there is a running observer at the given location. Intended
   * for testing.
   */
  public abstract boolean hasObserver(File file);
}
