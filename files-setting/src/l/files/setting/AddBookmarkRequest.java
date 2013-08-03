package l.files.setting;

import java.io.File;

/**
 * Event representing a request for a bookmark to be added.
 */
public final class AddBookmarkRequest extends Value<File> {

  public AddBookmarkRequest(File file) {
    super(file);
  }
}
