package l.files.fs;

import com.google.common.net.MediaType;

import java.io.IOException;


/**
 * Detects the content type of a file, if the file is a link returns the content
 * type of the target file.
 */
public interface FileTypeDetector {

  MediaType detect(Path path) throws IOException;

  MediaType detect(FileStatus status) throws IOException;

}
