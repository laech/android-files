package l.files.common.io;

import java.io.IOException;
import java.io.InputStream;

public interface Detector {

  /**
   * Detects the media type of the given stream.
   *
   * @return the media type, or {@code "application/octet-stream"} if unknown.
   */
  String detect(InputStream stream) throws IOException;
}
