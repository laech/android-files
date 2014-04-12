package l.files.fse;

import com.google.auto.value.AutoValue;

import l.files.io.Path;

@AutoValue
public abstract class WatchEvent {
  WatchEvent() {}

  public abstract Kind kind();
  public abstract Path path();

  public static WatchEvent create(Kind kind, Path path) {
    return new AutoValue_WatchEvent(kind, path);
  }

  public static enum Kind {
    CREATE, DELETE, MODIFY
  }

  /**
   * Listener to be notified when files are being added/changed/removed from a
   * monitored directory.
   * <p/>
   * Note that when a listener method is called, the target file may have
   * already be changed again.
   * <p/>
   * Methods defined in this listener will be called from a background thread,
   * and expensive operations should be moved out of the thread to avoid
   * blocking of events to other listeners.
   */
  public static interface Listener {
    void onEvent(WatchEvent event);
  }
}
