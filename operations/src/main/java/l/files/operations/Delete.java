package l.files.operations;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.FileSystem;
import l.files.fs.Path;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Delete extends AbstractOperation {

    private final AtomicInteger deletedItemCount = new AtomicInteger();
    private final AtomicLong deletedByteCount = new AtomicLong();

    Delete(Map<Path, FileSystem> sourcePaths) {
        super(sourcePaths);
    }

    public int getDeletedItemCount() {
        return deletedItemCount.get();
    }

    public long getDeletedByteCount() {
        return deletedByteCount.get();
    }

    @Override
    void process(final FileSystem fs, final Path path) throws InterruptedException {
        traverse(fs, path, new OperationVisitor() {

            @Override
            public Result onPostVisit(Path path) throws IOException {
                delete(fs, path);
                return super.onPostVisit(path);
            }

        });
    }

    private void delete(FileSystem fs, Path path) throws IOException {
        long size = fs.stat(path, NOFOLLOW).size();
        fs.delete(path);
        deletedByteCount.addAndGet(size);
        deletedItemCount.incrementAndGet();
    }

}
