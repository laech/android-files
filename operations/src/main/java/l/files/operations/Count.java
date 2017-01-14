package l.files.operations;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.FileSystem;
import l.files.fs.Path;

class Count extends AbstractOperation {

    private final AtomicInteger count = new AtomicInteger();

    Count(Map<? extends Path, ? extends FileSystem> sourcePaths) {
        super(sourcePaths);
    }

    public int getCount() {
        return count.get();
    }

    @Override
    void process(final FileSystem fs, final Path path) throws InterruptedException {
        traverse(fs, path, new OperationVisitor() {

            @Override
            public Result onPreVisit(Path path) throws IOException {
                count.incrementAndGet();
                onCount(fs, path);
                return super.onPreVisit(path);
            }

        });
    }

    void onCount(FileSystem fs, Path path) {
    }

}
