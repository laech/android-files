package l.files.operations;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Path;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Delete extends AbstractOperation {

    private final AtomicInteger deletedItemCount = new AtomicInteger();
    private final AtomicLong deletedByteCount = new AtomicLong();

    Delete(Set<? extends Path> sourcePaths) {
        super(sourcePaths);
    }

    int getDeletedItemCount() {
        return deletedItemCount.get();
    }

    long getDeletedByteCount() {
        return deletedByteCount.get();
    }

    @Override
    void process(final Path path) throws InterruptedException {
        traverse(path, new OperationVisitor() {

            @Override
            public Result onPostVisit(Path path) throws IOException {
                delete(path);
                return super.onPostVisit(path);
            }

        });
    }

    private void delete(Path path) throws IOException {
        long size = path.stat(NOFOLLOW).size();
        path.delete();
        deletedByteCount.addAndGet(size);
        deletedItemCount.incrementAndGet();
    }

}
