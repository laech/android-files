package l.files.operations;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.FileSystem;
import l.files.fs.Path;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Size extends Count {

    private final AtomicLong size = new AtomicLong();

    Size(Map<Path, FileSystem> sourcePaths) {
        super(sourcePaths);
    }

    public long getSize() {
        return size.get();
    }

    @Override
    void onCount(FileSystem fs, Path path) {
        super.onCount(fs, path);
        try {
            size.addAndGet(fs.stat(path, NOFOLLOW).size());
        } catch (IOException e) {
            // Ignore count
        }
    }

}
