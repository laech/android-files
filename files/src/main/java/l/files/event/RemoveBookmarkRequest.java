package l.files.event;

import java.io.File;

/**
 * Event representing a request for a bookmark to be removed.
 */
public final class RemoveBookmarkRequest extends FileEvent {

  public RemoveBookmarkRequest(File file) {
    super(file);
  }

}
