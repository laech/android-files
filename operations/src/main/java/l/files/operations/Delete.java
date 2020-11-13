package l.files.operations;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.Files.readAttributes;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

final class Delete extends AbstractOperation {

    private final AtomicInteger deletedItemCount = new AtomicInteger();
    private final AtomicLong deletedByteCount = new AtomicLong();

    Delete(Collection<? extends Path> sourcePaths) {
        super(sourcePaths);
    }

    int getDeletedItemCount() {
        return deletedItemCount.get();
    }

    long getDeletedByteCount() {
        return deletedByteCount.get();
    }

    @Override
    void process(Path path) {
        traverse(path, new OperationVisitor() {

            @Override
            public FileVisitResult visitFile(
                Path file,
                BasicFileAttributes attrs
            ) throws IOException {
                delete(file);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                throws IOException {
                if (e == null) {
                    delete(dir);
                }
                return super.postVisitDirectory(dir, e);
            }

        });
    }

    private void delete(Path path) throws IOException {
        long size = readAttributes(
            path,
            BasicFileAttributes.class,
            NOFOLLOW_LINKS
        ).size();
        Files.delete(path);
        deletedByteCount.addAndGet(size);
        deletedItemCount.incrementAndGet();
    }

}
