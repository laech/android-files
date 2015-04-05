package l.files.operations;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import kotlin.Function2;
import kotlin.Unit;
import l.files.fs.Path;
import l.files.fs.Resource;

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
    void process(Path path, FailureRecorder listener) {
        try {
            deleteTree(path, listener);
        } catch (IOException e) {
            listener.onFailure(path, e);
        }
    }

    private void deleteTree(Path path, final FailureRecorder listener) throws IOException {
        Iterator<Resource> it = path.getResource().traverse(POST_ORDER, new Function2<Resource, IOException, Unit>() {
            @Override
            public Unit invoke(Resource resource, IOException e) {
                listener.onFailure(resource.getPath(), e);
                return null;
            }
        }).iterator();
        while (it.hasNext() && !Thread.currentThread().isInterrupted()) {
            Resource resource = it.next();
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
