package l.files.event;

import java.io.File;

/**
 * Event representing a request to open a file.
 */
public final class OpenFileRequest extends FileEvent {

  public OpenFileRequest(File file) {
    super(file);
  }

}