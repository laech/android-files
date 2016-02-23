package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Name;
import l.files.fs.Path;

import static l.files.fs.Files.move;

final class Move extends Paste {

    private final AtomicInteger movedItemCount = new AtomicInteger();

    Move(Path sourceDirectory, Collection<? extends Name> sourceFiles, Path destination) {
        super(sourceDirectory, sourceFiles, destination);
    }

    public int getMovedItemCount() {
        return movedItemCount.get();
    }

    @Override
    void paste(Path from, Path to) {
        try {
            move(from, to);
            movedItemCount.incrementAndGet();
        } catch (IOException e) {
            record(from.parent(), from.name(), e);
        }
    }

}
