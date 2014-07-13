package l.files.io.file.operations;

import java.io.IOException;
import java.util.List;

import l.files.io.file.DirectoryTreeTraverser;
import l.files.io.file.FileInfo;
import l.files.logging.Logger;

import static l.files.io.file.DirectoryTreeTraverser.Entry;
import static l.files.io.file.Files.remove;
import static l.files.io.file.operations.FileOperations.checkInterrupt;

public final class Delete extends AbstractOperation {

    private static final Logger logger = Logger.get(Delete.class);

    private volatile int deletedItemCount;
    private volatile long deletedByteCount;

    public Delete(Iterable<String> paths) {
        super(paths);
    }

    /**
     * Gets the number of items deleted so far.
     */
    public int getDeletedItemCount() {
        return deletedItemCount;
    }

    /**
     * Gets the number of bytes deleted so far.
     */
    public long getDeletedByteCount() {
        return deletedByteCount;
    }

    @Override
    protected void process(String path, List<Failure> failures) throws InterruptedException {
        deleteTree(path, failures);
    }

    private void deleteTree(String path, List<Failure> failures) throws InterruptedException {
        Entry root = Entry.create(path);
        for (Entry entry : DirectoryTreeTraverser.get().postOrderTraversal(root)) {
            checkInterrupt();
            try {
                delete(entry.path());
            } catch (IOException e) {
                failures.add(Failure.create(entry.path(), e));
                logger.warn(e);
            }
        }
    }

    private void delete(String path) throws IOException {
        long size = FileInfo.get(path).getSize();
        remove(path);
        deletedByteCount += size;
        deletedItemCount++;
    }
}
