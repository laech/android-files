package l.files.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

final class MonitoringCursor extends CursorWrapper {

  private final Monitor monitor;

  private MonitoringCursor(Cursor delegate, Monitor monitor) {
    super(delegate);
    this.monitor = monitor;
  }

  static MonitoringCursor create(
      ContentResolver resolver, Uri uri, File dir, Cursor delegate) {

    checkNotNull(dir, "dir");
    checkNotNull(resolver, "resolver");
    checkNotNull(uri, "uri");
    checkNotNull(delegate, "delegate");

    Monitor monitor = newMonitor(dir, resolver, uri);
    monitor.start();
    return new MonitoringCursor(delegate, monitor);
  }


  private static Monitor newMonitor(
      final File directory, final ContentResolver resolver, final Uri uri) {
    return new Monitor(directory, new Runnable() {
      @Override public void run() {
        resolver.notifyChange(uri, null);
      }
    });
  }

  @Override public void close() {
    monitor.stop();
    super.close();
  }
}
