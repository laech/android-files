package l.files.operations;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

class Count extends AbstractOperation {

    private final AtomicInteger count = new AtomicInteger();

    Count(Collection<? extends Path> sourcePaths) {
        super(sourcePaths);
    }

    int getCount() {
        return count.get();
    }

    @Override
    void process(Path path) {
        traverse(path, new OperationVisitor() {

            @Override
            public FileVisitResult preVisitDirectory(
                Path dir, BasicFileAttributes attrs
            ) throws IOException {
                count(attrs);
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult visitFile(
                Path file, BasicFileAttributes attrs
            ) throws IOException {
                count(attrs);
                return super.visitFile(file, attrs);
            }

            private void count(BasicFileAttributes attrs) {
                count.incrementAndGet();
                onCount(attrs);
            }
        });
    }

    void onCount(BasicFileAttributes attrs) {
    }

}
