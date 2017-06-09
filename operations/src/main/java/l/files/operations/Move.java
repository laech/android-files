package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;

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
            sourcePath.rename(destinationPath);
            movedItemCount.incrementAndGet();
        } catch (IOException e) {
            record(sourcePath, e);
        }
    }

}
