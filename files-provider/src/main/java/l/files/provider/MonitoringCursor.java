package l.files.provider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

final class MonitoringCursor extends CursorWrapper implements Runnable {

  private final File dir;
  private final Uri uri;
  private final ContentResolver resolver;

  private MonitoringCursor(
      Cursor delegate, ContentResolver resolver, Uri uri, File dir) {
    super(delegate);
    this.dir = checkNotNull(dir, "dir");
    this.uri = checkNotNull(uri, "uri");
    this.resolver = checkNotNull(resolver, "resolver");
  }

  static MonitoringCursor create(
      ContentResolver resolver, Uri uri, File dir, Cursor cursor) {

    checkNotNull(dir, "dir");
    checkNotNull(resolver, "resolver");
    checkNotNull(uri, "uri");
    checkNotNull(cursor, "cursor");

    MonitoringCursor monitor = new MonitoringCursor(cursor, resolver, uri, dir);
    Monitor.INSTANCE.register(dir, monitor);
    return monitor;
  }

  @Override public void close() {
    super.close();
    Monitor.INSTANCE.unregister(dir, this);
  }

  @Override public void run() {
    resolver.notifyChange(uri, null);
  }
}
