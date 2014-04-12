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
  void onEvent(WatchEvent event);
}
