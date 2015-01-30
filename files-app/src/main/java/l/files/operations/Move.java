package l.files.operations;

import java.io.File;
import java.io.IOException;

import l.files.fs.Path;

import static l.files.fs.local.Files.rename;

final class Move extends Paste {

  private volatile int movedItemCount;

  public Move(Iterable<Path> sources, Path dstDir) {
    super(sources, dstDir);
  }

  /**
   * Gets the number of items moved so far.
   */
  public int getMovedItemCount() {
    return movedItemCount;
  }

  @Override void paste(Path from, Path to, FailureRecorder listener) {
    try {
      rename(new File(from.getUri()).getPath(), new File(to.getUri()).getPath()); // TODO fix this
      movedItemCount++;
    } catch (IOException e) {
      listener.onFailure(from, e);
    }
  }
}
