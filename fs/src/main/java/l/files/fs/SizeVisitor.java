package l.files.fs;

import java.io.IOException;

public interface SizeVisitor {

    /**
     * Called per file/directory.
     *
     * @param size       the size of the file/directory in bytes
     * @param sizeOnDisk the size of actual storage used in bytes
     */
    boolean onSize(long size, long sizeOnDisk)
            throws IOException;

}
