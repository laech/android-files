package l.files.operations;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;
import l.files.fs.Visitor;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

final class Delete extends AbstractOperation implements Visitor
{

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
            postOrderTraversal(resource, this);
        } catch (IOException e) {
            record(resource, e);
        }
    }

    @Override
    public Result accept(Resource resource) {
        if (isInterrupted()) {
            return TERMINATE;
        }
        try {
            delete(resource);
        } catch (IOException e) {
            record(resource, e);
        }
        return CONTINUE;
    }

    private void delete(Resource resource) throws IOException {
        long size = resource.stat(NOFOLLOW).size();
        resource.delete();
        deletedByteCount.addAndGet(size);
        deletedItemCount.incrementAndGet();
    }

}
