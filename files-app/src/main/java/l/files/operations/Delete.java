package l.files.operations;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;

import static l.files.fs.Resource.Stream;
import static l.files.fs.Resource.TraversalOrder.POST_ORDER;

final class Delete extends AbstractOperation {

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
    void process(Resource resource, FailureRecorder listener) throws InterruptedException {
        try {
            deleteTree(resource, listener);
        } catch (IOException e) {
            listener.onFailure(resource, e);
        }
    }

    private void deleteTree(Resource resource, FailureRecorder listener) throws IOException, InterruptedException {
        try (Stream resources = traverse(resource, POST_ORDER, listener)) {
            for (Resource child : resources) {
                checkInterrupt();
                try {
                    delete(child);
                } catch (IOException e) {
                    listener.onFailure(child, e);
                }
            }
        }
    }

    private void delete(Resource resource) throws IOException {
        long size = resource.readStatus(false).getSize();
        resource.delete();
        deletedByteCount.addAndGet(size);
        deletedItemCount.incrementAndGet();
    }

}
