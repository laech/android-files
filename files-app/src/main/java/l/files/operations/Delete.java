package l.files.operations;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;
import l.files.fs.ResourceVisitor;

import static l.files.fs.ResourceVisitor.Order.POST;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;
import static l.files.fs.ResourceVisitor.Result.TERMINATE;

final class Delete extends AbstractOperation implements ResourceVisitor {

    private final AtomicInteger deletedItemCount = new AtomicInteger();
    private final AtomicLong deletedByteCount = new AtomicLong();

    Delete(Iterable<? extends Resource> resources) {
        super(resources);
    }

    public int getDeletedItemCount() {
        return deletedItemCount.get();
    }

    public long getDeletedByteCount() {
        return deletedByteCount.get();
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
    public Result accept(Order order, Resource resource) {
        if (isInterrupted()) {
            return TERMINATE;
        }
        if (POST.equals(order)) {
            try {
                delete(resource);
            } catch (IOException e) {
                record(resource, e);
            }
        }
        return CONTINUE;
    }

    private void delete(Resource resource) throws IOException {
        long size = resource.readStatus(false).getSize();
        resource.delete();
        deletedByteCount.addAndGet(size);
        deletedItemCount.incrementAndGet();
    }

}
