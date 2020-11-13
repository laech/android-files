package l.files.operations;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.Files.move;

final class Move extends Paste {

    private final AtomicInteger movedItemCount = new AtomicInteger();

    Move(Collection<? extends Path> sourcePaths, Path destinationPath) {
        super(sourcePaths, destinationPath);
    }

    int getMovedItemCount() {
        return movedItemCount.get();
    }

    @Override
    void paste(Path sourcePath, Path destinationPath) throws IOException {
        try {
            move(sourcePath, destinationPath);
            movedItemCount.incrementAndGet();
        } catch (IOException e) {
            record(sourcePath, e);
        }
    }

}
