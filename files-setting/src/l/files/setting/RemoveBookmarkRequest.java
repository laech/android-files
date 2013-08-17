package l.files.setting;

import java.io.File;
import l.files.common.base.Value;

/**
 * Event representing a request for a bookmark to be removed.
 */
public final class RemoveBookmarkRequest extends Value<File> {

  public RemoveBookmarkRequest(File file) {
    super(file);
  }
}
