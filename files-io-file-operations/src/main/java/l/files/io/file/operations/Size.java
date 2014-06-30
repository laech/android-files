package l.files.io.file.operations;

import java.io.IOException;

import l.files.io.file.FileInfo;
import l.files.logging.Logger;

public final class Size extends Count {

    private static final Logger logger = Logger.get(Size.class);

    private volatile long size;

    public Size(Iterable<String> paths) {
        super(paths);
    }

    /**
     * Gets the size in bytes counted so far.
     */
    public long getSize() {
        return size;
    }

    @Override
    protected void onCount(String path) {
        super.onCount(path);
        try {
            size += FileInfo.get(path).getSize();
        } catch (IOException e) {
            logger.warn(e);
        }
    }
}
