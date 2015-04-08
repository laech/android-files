package l.files.operations;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;

final class Size extends Count {

    private final AtomicLong size = new AtomicLong();

    Size(Iterable<? extends Resource> resources) {
        super(resources);
    }

    public long getSize() {
        return size.get();
    }

    @Override
    void onCount(Resource resource) {
        super.onCount(resource);
        try {
            size.addAndGet(resource.readStatus(false).getSize());
        } catch (IOException e) {
            // Ignore count
        }
    }

}
