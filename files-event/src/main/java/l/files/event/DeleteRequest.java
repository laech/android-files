package l.files.event;

import java.io.File;
import l.files.common.base.Value;

public final class DeleteRequest extends Value<File> {

  public DeleteRequest(File file) {
    super(file);
  }
}
