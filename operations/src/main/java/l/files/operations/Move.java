package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;

import static l.files.fs.Files.move;

final class Move extends Paste {

    private final AtomicInteger movedItemCount = new AtomicInteger();

    Move(Collection<? extends Path> sources, Path destination) {
        super(sources, destination);
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
            record(from, e);
        }
    }

}
