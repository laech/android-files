package l.files.operations;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;

class Count extends AbstractOperation {

    private final AtomicInteger count = new AtomicInteger();

    Count(Set<? extends Path> sourcePaths) {
        super(sourcePaths);
    }

    public int getCount() {
        return count.get();
    }

    @Override
    void process(final Path path) throws InterruptedException {
        traverse(path, new OperationVisitor() {

            @Override
            public Result onPreVisit(Path path) throws IOException {
                count.incrementAndGet();
                onCount(path);
                return super.onPreVisit(path);
            }

        });
    }

    void onCount(Path path) {
    }

}
