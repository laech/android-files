package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Path;

import static l.files.fs.Files.stat;
import static l.files.fs.LinkOption.NOFOLLOW;

final class Size extends Count {

    private final AtomicLong size = new AtomicLong();

    Size(Collection<? extends Path> paths) {
        super(paths);
    }

    public long getSize() {
        return size.get();
    }

    @Override
    void onCount(Path path) {
        super.onCount(path);
        try {
            size.addAndGet(stat(path, NOFOLLOW).size());
        } catch (IOException e) {
            // Ignore count
        }
    }

}
