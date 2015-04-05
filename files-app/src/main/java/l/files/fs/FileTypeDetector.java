package l.files.fs;

import com.google.common.net.MediaType;

import java.io.IOException;

public interface FileTypeDetector {

    /**
     * Detects the content type of a file, if the file is a link returns the
     * content type of the target file.
     */
    MediaType detect(Path path) throws IOException;

    /**
     * Detects the content type of a file, use an existing status as hint.
     */
    MediaType detect(ResourceStatus status) throws IOException;

}
