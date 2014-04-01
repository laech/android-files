package l.files.fse;

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
   * @param event the original event type
   * @param parent the path of the parent directory
   * @param child the name of the child file
   */
  void onFileAdded(int event, String parent, String child);

  /**
   * A new file's attribute has changed.
   *
   * @param event the original event type
   * @param parent the path of the parent directory
   * @param child the name of the child file
   */
  void onFileChanged(int event, String parent, String child);

  /**
   * A new file has been deleted or moved out from a directory.
   *
   * @param event the original event type
   * @param parent the path of the parent directory
   * @param child the name of the child file
   */
  void onFileRemoved(int event, String parent, String child);
}
