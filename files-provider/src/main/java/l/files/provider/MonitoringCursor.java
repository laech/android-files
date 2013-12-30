package l.files.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A cursor that listens to change events on a directory (non-recursive), and
 * notifies the content resolver with the specified URI when an event occurs.
 */
final class MonitoringCursor extends CursorWrapper implements Runnable {

  private final String directory;
  private final Uri uri;
  private final ContentResolver resolver;

  private MonitoringCursor(
      Cursor delegate,
      ContentResolver resolver,
      Uri uri,
      String directoryPath) {
    super(delegate);
    this.directory = checkNotNull(directoryPath, "directoryPath");
    this.uri = checkNotNull(uri, "uri");
    this.resolver = checkNotNull(resolver, "resolver");
  }

  /**
   * @see Monitor#register(String, Set, Runnable)
   */
  static MonitoringCursor create(
      ContentResolver resolver,
      Uri uri,
      String directoryPath,
      Set<String> subDirectoryPaths,
      Cursor cursor) {

    checkNotNull(directoryPath, "directoryPath");
    checkNotNull(resolver, "resolver");
    checkNotNull(uri, "uri");
    checkNotNull(cursor, "cursor");

    MonitoringCursor monitor = new MonitoringCursor(cursor, resolver, uri, directoryPath);
    Monitor.INSTANCE.register(directoryPath, subDirectoryPaths, monitor);
    return monitor;
  }

  @Override public void close() {
    super.close();
    Monitor.INSTANCE.unregister(directory, this);
  }

  @Override public void run() {
    resolver.notifyChange(uri, null);
  }
}
