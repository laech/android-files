package l.files.io.file.operations;

import java.util.List;

import l.files.io.file.DirectoryTreeTraverser;

import static l.files.io.file.DirectoryTreeTraverser.Entry;
import static l.files.io.file.operations.FileOperations.checkInterrupt;

public class Count extends AbstractOperation {

    private volatile int count;

    public Count(Iterable<String> paths) {
        super(paths);
    }

    /**
     * Gets the number of items counted so far.
     */
    public int getCount() {
        return count;
    }

    @Override
    protected void process(String path, List<Failure> failures) throws InterruptedException {
        count(path);
    }

    private void count(String path) throws InterruptedException {
        Entry root = Entry.create(path);
        for (Entry entry : DirectoryTreeTraverser.get().breadthFirstTraversal(root)) {
            checkInterrupt();
            count++;
            onCount(entry.path());
        }
    }

    protected void onCount(String path) {
    }
}
