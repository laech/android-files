package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Name;
import l.files.fs.Path;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Delete extends AbstractOperation {

    private final AtomicInteger deletedItemCount = new AtomicInteger();
    private final AtomicLong deletedByteCount = new AtomicLong();

    Delete(Path sourceDirectory, Collection<? extends Name> sourceFiles) {
        super(sourceDirectory, sourceFiles);
    }

    public int getDeletedItemCount() {
        return deletedItemCount.get();
    }

    public long getDeletedByteCount() {
        return deletedByteCount.get();
    }

    @Override
    void process(Path sourceDirectory, Name sourceFile) throws InterruptedException {
        traverse(sourceDirectory, sourceFile, new OperationVisitor() {

            @Override
            public Result onPostVisit(Path path) throws IOException {
                delete(path);
                return super.onPostVisit(path);
            }

        });
    }

    private void delete(Path path) throws IOException {
        long size = l.files.fs.Files.stat(path, NOFOLLOW).size();
        l.files.fs.Files.delete(path);
        deletedByteCount.addAndGet(size);
        deletedItemCount.incrementAndGet();
    }

}
