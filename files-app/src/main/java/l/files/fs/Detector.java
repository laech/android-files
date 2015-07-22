package l.files.fs;

import java.io.IOException;

public interface Detector {

  String OCTET_STREAM = "application/octet-stream";
  String ANY_TYPE = "*/*";

  /**
   * Detects the content type of a file, if the file is a link returns the
   * content type of the target file.
   */
  String detect(Resource resource) throws IOException;

  /**
   * Detects the content type of a file, use an existing status as hint.
   */
  String detect(Resource resource, Stat stat) throws IOException;

}
