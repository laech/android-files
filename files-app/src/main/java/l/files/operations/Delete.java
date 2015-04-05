package l.files.operations;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Path;
import l.files.fs.Resource;

import static l.files.fs.Resource.TraversalExceptionHandler;
import static l.files.fs.Resource.TraversalOrder.POST_ORDER;

final class Delete extends AbstractOperation {

    private final AtomicInteger deletedItemCount = new AtomicInteger();
    private final AtomicLong deletedByteCount = new AtomicLong();

    Delete(Iterable<? extends Path> paths) {
        super(paths);
    }

    public int getDeletedItemCount() {
        return deletedItemCount.get();
    }

    public long getDeletedByteCount() {
        return deletedByteCount.get();
    }

    @Override
    void process(Path path, FailureRecorder listener) throws InterruptedException {
        try {
            deleteTree(path, listener);
        } catch (IOException e) {
            listener.onFailure(path, e);
        }
    }

    private void deleteTree(Path path, final FailureRecorder listener) throws IOException, InterruptedException {
        Iterable<Resource> resources = path.getResource().traverse(POST_ORDER, new TraversalExceptionHandler() {
            @Override
            public void handle(Resource resource, IOException e) {
                listener.onFailure(resource.getPath(), e);
            }
        });
        for (Resource resource : resources) {
            checkInterrupt();
            try {
                delete(resource);
            } catch (IOException e) {
                listener.onFailure(resource.getPath(), e);
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
