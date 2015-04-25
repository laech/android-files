package l.files.operations;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Resource;

final class Move extends Paste {

    private final AtomicInteger movedItemCount = new AtomicInteger();

    Move(Iterable<? extends Resource> sources, Resource destination) {
        super(sources, destination);
    }

    public int getMovedItemCount() {
        return movedItemCount.get();
    }

    @Override
    void paste(Resource from, Resource to) {
        try {
            from.moveTo(to);
            movedItemCount.incrementAndGet();
        } catch (IOException e) {
            record(from, e);
        }
    }

}
