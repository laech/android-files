package l.files.operations;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.FileSystem;
import l.files.fs.Path;

final class Move extends Paste {

    private final AtomicInteger movedItemCount = new AtomicInteger();

    Move(
            Map<? extends Path, ? extends FileSystem> sourcePaths,
            FileSystem destinationFs,
            Path destinationPath
    ) {
        super(sourcePaths, destinationFs, destinationPath);
    }

    public int getMovedItemCount() {
        return movedItemCount.get();
    }

    @Override
    void paste(
            FileSystem sourceFs,
            Path sourcePath,
            FileSystem destinationFs,
            Path destinationPath
    ) throws IOException {
        try {
            if (!sourceFs.equals(destinationFs)) {
                throw new IOException(sourceFs + " != " + destinationFs);
            }
            destinationFs.move(sourcePath, destinationPath);
            movedItemCount.incrementAndGet();
        } catch (IOException e) {
            record(sourcePath, e);
        }
    }

}
