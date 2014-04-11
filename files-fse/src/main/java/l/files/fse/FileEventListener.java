package l.files.fse;

import l.files.io.Path;

/**
 * Listener to be notified when files are being added/changed/removed from a
 * monitored directory.
 * <p/>
 * Note that when a listener method is called, the target file may have already
 * be changed again.
 * <p/>
 * Methods defined in this listener will be called from a background thread, and
 * expensive operations should be moved out of the thread to avoid blocking of
 * events to other listeners.
 */
public interface FileEventListener {

  /**
   * A new file has been added or moved to a directory.
   *
   * @param event the original {@link android.os.FileObserver} event type
   * @param path the path that was added to its parent
   */
  void onFileAdded(int event, Path path);

  /**
   * A new file's attribute has changed.
   *
   * @param event the original {@link android.os.FileObserver} event type
   * @param path the path that was changed
   */
  void onFileChanged(int event, Path path);

  /**
   * A new file has been deleted or moved out from a directory.
   *
   * @param event the original {@link android.os.FileObserver} event type
   * @param path the path that was removed from its parent
   */
  void onFileRemoved(int event, Path path);
}
