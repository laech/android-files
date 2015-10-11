package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.File;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Delete extends AbstractOperation {

    private final AtomicInteger deletedItemCount = new AtomicInteger();
    private final AtomicLong deletedByteCount = new AtomicLong();

    Delete(Collection<? extends File> files) {
        super(files);
    }

    public int getDeletedItemCount() {
        return deletedItemCount.get();
    }

    public long getDeletedByteCount() {
        return deletedByteCount.get();
    }

    @Override
    void process(File file) {
        traverse(file, new OperationVisitor() {

            @Override
            public Result onPostVisit(File file) throws IOException {
                delete(file);
                return super.onPostVisit(file);
            }

        });
    }

    private void delete(File file) throws IOException {
        long size = file.stat(NOFOLLOW).size();
        file.delete();
        deletedByteCount.addAndGet(size);
        deletedItemCount.incrementAndGet();
    }

}