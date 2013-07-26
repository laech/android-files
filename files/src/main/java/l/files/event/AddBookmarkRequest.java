package l.files.event;

import java.io.File;

/**
 * Event representing a request for a bookmark to be added.
 */
public final class AddBookmarkRequest extends FileEvent {

  public AddBookmarkRequest(File file) {
    super(file);
  }

}
