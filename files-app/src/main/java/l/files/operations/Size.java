package l.files.operations;

import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.local.LocalResourceStatus;
import l.files.logging.Logger;

final class Size extends Count {

  private static final Logger logger = Logger.get(Size.class);

  private volatile long size;

  public Size(Iterable<? extends Path> paths) {
    super(paths);
  }

  /**
   * Gets the size in bytes counted so far.
   */
  public long getSize() {
    return size;
  }

  @Override protected void onCount(Path path) {
    super.onCount(path);
    try {
      size += LocalResourceStatus.stat(path, false).getSize();
    } catch (IOException e) {
      logger.warn(e);
    }
  }
}
