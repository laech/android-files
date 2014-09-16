package l.files.operations;

import java.io.IOException;

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

  @Override void paste(String from, String to, FailureRecorder listener) {
    try {
      rename(from, to);
      movedItemCount++;
    } catch (IOException e) {
      listener.onFailure(from, e);
    }
  }
}
