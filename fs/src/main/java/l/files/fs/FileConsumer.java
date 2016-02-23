package l.files.fs;

import java.io.IOException;

public interface FileConsumer {

    /**
     * @return true to continue, false to stop for multi-item callbacks
     */
    boolean accept(Path parent, Name child) throws IOException;

}
