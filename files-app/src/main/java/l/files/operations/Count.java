package l.files.operations;


import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import kotlin.Function2;
import kotlin.Unit;
import l.files.fs.Path;
import l.files.fs.Resource;

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
    void process(Path path, FailureRecorder listener) {
        try {
            count(path, listener);
        } catch (IOException e) {
            listener.onFailure(path, e);
        }
    }

    private void count(Path path, final FailureRecorder listener) throws IOException {
        Iterator<Resource> it = path.getResource().traverse(BREATH_FIRST, new Function2<Resource, IOException, Unit>() {
            @Override
            public Unit invoke(Resource resource, IOException e) {
                listener.onFailure(resource.getPath(), e);
                return null;
            }
        }).iterator();
        while (it.hasNext() && !Thread.currentThread().isInterrupted()) {
            count.incrementAndGet();
            onCount(it.next());
        }
    }

    void onCount(Resource resource) {
    }

}
