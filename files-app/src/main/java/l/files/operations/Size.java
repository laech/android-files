package l.files.operations;

import l.files.fs.FileSystemException;
import l.files.fs.local.LocalFileStatus;
import l.files.fs.local.LocalPath;
import l.files.logging.Logger;

final class Size extends Count {

  private static final Logger logger = Logger.get(Size.class);

  private volatile long size;

  public Size(Iterable<String> paths) {
    super(paths);
  }

  /**
   * Gets the size in bytes counted so far.
   */
  public long getSize() {
    return size;
  }

  @Override protected void onCount(String path) {
    super.onCount(path);
    try {
      size += LocalFileStatus.stat(LocalPath.of(path), false).size();
    } catch (FileSystemException e) {
      logger.warn(e);
    }
  }
}
