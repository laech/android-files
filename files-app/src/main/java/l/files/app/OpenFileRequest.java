package l.files.app;

import android.database.Cursor;

import com.google.auto.value.AutoValue;

import l.files.provider.FileCursors;

@AutoValue
abstract class OpenFileRequest {

  OpenFileRequest() {
  }

  public abstract String fileLocation();

  public abstract String filename();

  public abstract boolean canRead();

  public abstract boolean isDirectory();

  public static OpenFileRequest create(
      String location, String filename, boolean canRead, boolean directory) {
    return new AutoValue_OpenFileRequest(location, filename, canRead, directory);
  }

  public static OpenFileRequest from(Cursor cursor) {
    return create(
        FileCursors.getLocation(cursor),
        FileCursors.getName(cursor),
        FileCursors.isReadable(cursor),
        FileCursors.isDirectory(cursor));
  }
}
