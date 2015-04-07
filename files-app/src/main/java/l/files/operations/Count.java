package l.files.operations;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;
import l.files.fs.Resource;

import static l.files.fs.Resource.ResourceStream;
import static l.files.fs.Resource.TraversalOrder.BREATH_FIRST;

class Count extends AbstractOperation {

    private final AtomicInteger count = new AtomicInteger();

    Count(Iterable<? extends Path> paths) {
        super(paths);
    }

    public int getCount() {
        return count.get();
    }

    @Override
    void process(Path path, FailureRecorder listener) throws InterruptedException {
        try {
            count(path, listener);
        } catch (IOException e) {
            listener.onFailure(path, e);
        }
    }

    private void count(Path path, final FailureRecorder listener) throws IOException, InterruptedException {
        try (ResourceStream resources = traverse(path, BREATH_FIRST, listener)) {
            for (Resource resource : resources) {
                checkInterrupt();
                onCount(resource);
                count.incrementAndGet();
            }
        }
    }

    void onCount(Resource resource) {
    }

}
