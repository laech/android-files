package l.files.operations;

import java.io.IOException;
import java.util.Collection;

import static l.files.io.file.Files.rename;

final class Move extends Paste {

  private volatile int movedItemCount;

  public Move(Iterable<String> sources, String dstDir) {
    super(sources, dstDir);
  }

  /**
   * Gets the number of items moved so far.
   */
  public int getMovedItemCount() {
    return movedItemCount;
  }

  @Override protected void paste(String from, String to, Collection<Failure> failures) {
    try {
      rename(from, to);
      movedItemCount++;
    } catch (IOException e) {
      failures.add(Failure.create(from, e));
    }
  }
}
