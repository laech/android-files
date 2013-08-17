package l.files.setting;

import java.io.File;
import l.files.common.base.Value;

/**
 * Event representing a request for a bookmark to be added.
 */
public final class AddBookmarkRequest extends Value<File> {

  public AddBookmarkRequest(File file) {
    super(file);
  }
}
