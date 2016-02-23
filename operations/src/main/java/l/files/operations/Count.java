package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Name;
import l.files.fs.Path;

class Count extends AbstractOperation {

    private final AtomicInteger count = new AtomicInteger();

    Count(Path sourceDirectory, Collection<? extends Name> sourceFiles) {
        super(sourceDirectory, sourceFiles);
    }

    public int getCount() {
        return count.get();
    }

    @Override
    void process(Path sourceDirectory, Name sourceFile) throws InterruptedException {
        traverse(sourceDirectory, sourceFile, new OperationVisitor() {

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
