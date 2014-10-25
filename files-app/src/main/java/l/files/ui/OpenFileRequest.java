package l.files.ui;

import android.database.Cursor;

import com.google.auto.value.AutoValue;

import static l.files.provider.FilesContract.Files;

@AutoValue
abstract class OpenFileRequest {

  OpenFileRequest() {
  }

  public abstract String fileId();

  public abstract String filename();

  public abstract boolean canRead();

  public abstract boolean isDirectory();

  public static OpenFileRequest create(
      String location, String filename, boolean canRead, boolean directory) {
    return new AutoValue_OpenFileRequest(location, filename, canRead, directory);
  }

  public static OpenFileRequest from(Cursor cursor) {
    return create(
        Files.id(cursor),
        Files.name(cursor),
        Files.isReadable(cursor),
        Files.isDirectory(cursor));
  }
}
