package l.files.fs;

import com.google.common.net.MediaType;

import java.io.IOException;

public interface ResourceTypeDetector {

    /**
     * Detects the content type of a file, if the file is a link returns the
     * content type of the target file.
     */
    MediaType detect(Resource resource) throws IOException;

    /**
     * Detects the content type of a file, use an existing status as hint.
     */
    MediaType detect(Resource resource,
                     ResourceStatus status) throws IOException;

}
