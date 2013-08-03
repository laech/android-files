package l.files.setting;

import java.io.File;

/**
 * Event representing a request for a bookmark to be removed.
 */
public final class RemoveBookmarkRequest extends Value<File> {

  public RemoveBookmarkRequest(File file) {
    super(file);
  }
}
