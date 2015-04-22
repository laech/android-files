package l.files.operations;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Resource;
import l.files.fs.ResourceVisitor;

import static l.files.fs.ResourceVisitor.Order.PRE;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;
import static l.files.fs.ResourceVisitor.Result.TERMINATE;

class Count extends AbstractOperation implements ResourceVisitor {

    private final AtomicInteger count = new AtomicInteger();

    Count(Iterable<? extends Resource> resources) {
        super(resources);
    }

    public int getCount() {
        return count.get();
    }

    @Override
    void process(Resource resource) {
        try {
            resource.traverse(this, this);
        } catch (IOException e) {
            record(resource, e);
        }
    }

    @Override
    public Result accept(Order order, Resource resource) throws IOException {
        if (isInterrupted()) {
            return TERMINATE;
        }
        if (PRE.equals(order)) {
            count.incrementAndGet();
            onCount(resource);
        }
        return CONTINUE;
    }

    void onCount(Resource resource) {
    }

}
