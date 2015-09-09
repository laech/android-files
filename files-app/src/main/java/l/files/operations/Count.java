package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.File;

class Count extends AbstractOperation {

    private final AtomicInteger count = new AtomicInteger();

    Count(Collection<? extends File> files) {
        super(files);
    }

    public int getCount() {
        return count.get();
    }

    @Override
    void process(File file) {
        traverse(file, new OperationVisitor() {

            @Override
            public Result onPreVisit(File file) throws IOException {
                count.incrementAndGet();
                onCount(file);
                return super.onPreVisit(file);
            }

        });
    }

    void onCount(File file) {
    }

}
