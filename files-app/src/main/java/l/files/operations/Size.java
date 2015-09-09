package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.File;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Size extends Count {

    private final AtomicLong size = new AtomicLong();

    Size(Collection<? extends File> files) {
        super(files);
    }

    public long getSize() {
        return size.get();
    }

    @Override
    void onCount(File file) {
        super.onCount(file);
        try {
            size.addAndGet(file.stat(NOFOLLOW).size());
        } catch (IOException e) {
            // Ignore count
        }
    }

}
