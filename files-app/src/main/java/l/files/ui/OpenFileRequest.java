package l.files.ui;

import com.google.auto.value.AutoValue;

import l.files.fs.FileStatus;

@AutoValue
abstract class OpenFileRequest {
  OpenFileRequest() {}

  public abstract FileStatus file();

  public static OpenFileRequest create(FileStatus file) {
    return new AutoValue_OpenFileRequest(file);
  }

}
