package l.files.operations;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Resource;

import static l.files.fs.Resource.TraversalOrder.BREATH_FIRST;

class Count extends AbstractOperation {

    private final AtomicInteger count = new AtomicInteger();

    Count(Iterable<? extends Resource> resources) {
        super(resources);
    }

    public int getCount() {
        return count.get();
    }

    @Override
    void process(Resource resource, FailureRecorder listener) throws InterruptedException {
        try {
            count(resource, listener);
        } catch (IOException e) {
            listener.onFailure(resource, e);
        }
    }

    private void count(Resource resource, FailureRecorder listener) throws IOException, InterruptedException {
        try (Resource.Stream resources = traverse(resource, BREATH_FIRST, listener)) {
            for (Resource child : resources) {
                checkInterrupt();
                onCount(child);
                count.incrementAndGet();
            }
        }
    }

    void onCount(Resource resource) {
    }

}
