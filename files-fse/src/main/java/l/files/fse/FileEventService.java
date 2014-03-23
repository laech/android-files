package l.files.fse;

import com.google.common.base.Optional;

import java.io.File;
import java.util.Map;

import l.files.os.Stat;

/**
 * TODO doc
 */
public abstract class FileEventService {
  FileEventService() {}

  public static FileEventService create() {
    return new FileEventServiceImpl();
  }

  /**
   * Starts monitoring on the given file path, file systems event on the given
   * path will be sent to all registered listeners.
   *
   * @return if this is the first time the path is being monitored, returns the
   * children information (if any)
   */
  public abstract Optional<Map<File, Stat>> monitor(File file); // TODO return something better

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

