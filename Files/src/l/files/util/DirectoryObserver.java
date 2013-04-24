package l.files.util;

import android.os.FileObserver;
import android.os.Handler;
import com.squareup.otto.Bus;

import java.io.File;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Notifies when the content of a directory is changed (adding/deleting/moving
 * files). Changes in subdirectories will not be notified.
 */
public class DirectoryObserver extends FileObserver {

  // http://code.google.com/p/android/issues/detail?id=33659

  private static final int MASK = CREATE | DELETE | MOVED_TO | MOVED_FROM;

  private final DirectoryChangedEvent event;
  private final Bus bus;
  private final Handler handler;

  public DirectoryObserver(File dir, Bus bus, Handler handler) {
    super(checkNotNull(dir, "dir").getAbsolutePath(), MASK);
    this.event = new DirectoryChangedEvent(dir);
    this.bus = checkNotNull(bus, "bus");
    this.handler = checkNotNull(handler, "handler");
  }

  @Override public void onEvent(int eventType, String path) {
    handler.post(new Runnable() {
      @Override public void run() {
        bus.post(event);
      }
    });
  }

  public static final class DirectoryChangedEvent {
    private final File directory;

    public DirectoryChangedEvent(File directory) {
      this.directory = checkNotNull(directory);
    }

    public File directory() {
      return directory;
    }

    @Override public boolean equals(Object o) {
      if (o instanceof DirectoryChangedEvent) {
        DirectoryChangedEvent that = (DirectoryChangedEvent) o;
        return directory().equals(that.directory());
      }
      return false;
    }

    @Override public int hashCode() {
      return directory().hashCode();
    }

    @Override public String toString() {
      return toStringHelper(this).addValue(directory()).toString();
    }
  }

}
