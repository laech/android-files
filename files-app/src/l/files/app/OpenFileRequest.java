package l.files.app;

import java.io.File;
import l.files.common.base.Value;

final class OpenFileRequest extends Value<File> {

  public OpenFileRequest(File file) {
    super(file);
  }
}