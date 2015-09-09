package l.files.operations;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.File;

final class Move extends Paste {

  private final AtomicInteger movedItemCount = new AtomicInteger();

  Move(Collection<? extends File> sources, File destination) {
    super(sources, destination);
  }

  public int getMovedItemCount() {
    return movedItemCount.get();
  }

  @Override void paste(File from, File to) {
    try {
      from.moveTo(to);
      movedItemCount.incrementAndGet();
    } catch (IOException e) {
      record(from, e);
    }
  }

}
