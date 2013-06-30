package l.files.io;

import java.io.File;

import com.google.common.base.Function;
import com.google.common.net.MediaType;

public interface MediaTypeDetector extends Function<File, MediaType> {

  /**
   * Detects the given files media type, returns {@link MediaType#OCTET_STREAM}
   * if unknown.
   */
  MediaType apply(File file);

}
