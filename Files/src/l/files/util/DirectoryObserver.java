package l.files.util;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.File;

import android.os.FileObserver;
import android.os.Handler;
import l.files.event.EventBus;

/**
 * Notifies when the content of a directory is changed (adding/deleting/moving
 * files). Changes in subdirectories will not be notified.
 */
public class DirectoryObserver extends FileObserver {

  // http://code.google.com/p/android/issues/detail?id=33659

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

  private final DirectoryChangedEvent event;
  private final EventBus bus;
  private final Handler handler;

  public DirectoryObserver(File directory, EventBus bus, Handler handler) {
    super(checkNotNull(directory, "directory").getAbsolutePath(), CREATE | DELETE | MOVED_TO | MOVED_FROM);
    this.event = new DirectoryChangedEvent(directory);
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

}
