package l.files.operations;


import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.Path;

final class Move extends Paste {

    private final AtomicInteger movedItemCount = new AtomicInteger();

    Move(Iterable<? extends Path> sources, Path dstPath) {
        super(sources, dstPath);
    }

    public int getMovedItemCount() {
        return movedItemCount.get();
    }

    @Override
    void paste(Path from, Path to, FailureRecorder listener) {
        try {
            from.getResource().move(to.getResource());
            movedItemCount.incrementAndGet();
        } catch (IOException e) {
            listener.onFailure(from, e);
        }
    }

}
