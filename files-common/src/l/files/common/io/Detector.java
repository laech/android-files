package l.files.common.io;

import com.google.common.net.MediaType;

import java.io.File;

public interface Detector {

  /**
   * Detects the media type of the given file.
   *
   * @return the media type, or {@link MediaType#OCTET_STREAM} if unknown.
   */
  MediaType detect(File file);
}
