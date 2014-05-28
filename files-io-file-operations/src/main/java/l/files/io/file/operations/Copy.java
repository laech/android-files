package l.files.io.file.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.util.Collection;

import l.files.io.file.DirectoryTreeTraverser;
import l.files.io.file.FileInfo;
import l.files.io.file.Files;
import l.files.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.Files.readlink;
import static l.files.io.file.Files.symlink;
import static l.files.io.file.operations.FileOperations.checkInterrupt;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.IOUtils.closeQuietly;

public final class Copy extends Paste {

  /*
   * Setting last modified time currently fails, see:
   * https://code.google.com/p/android/issues/detail?id=18624
   * https://code.google.com/p/android/issues/detail?id=34691
   * https://code.google.com/p/android/issues/detail?id=1992
   * https://code.google.com/p/android/issues/detail?id=1699
   * https://code.google.com/p/android/issues/detail?id=25460
   */

  private static final Logger logger = Logger.get(Copy.class);

  /* TODO check optimal size
   * Higher the buffer, faster the copy, but will affect the overall system
   * performance/responsiveness more.
   * Need to keep a good balance between speed and system performance.
   * This may be related:
   * http://stackoverflow.com/questions/4290679/why-high-io-rate-operations-slow-everything-on-linux
   */
  private static final long BUFFER_SIZE = 1024 * 4;

  private final Listener listener;

  public Copy(Listener listener, Iterable<String> sources, String dstDir) {
    super(sources, dstDir);
    this.listener = checkNotNull(listener, "listener");
  }

  @Override
  protected void paste(String from, String to, Collection<Failure> failures)
      throws InterruptedException {

    File oldRoot = new File(from);
    File newRoot = new File(to);

    for (String path : DirectoryTreeTraverser.get().preOrderTraversal(from)) {
      checkInterrupt();

      FileInfo file;
      try {
        file = FileInfo.get(path);
      } catch (IOException e) {
        logger.error(e);
        failures.add(Failure.create(path, e));
        continue;
      }

      File dst = Files.replace(new File(path), oldRoot, newRoot);
      if (file.isSymbolicLink()) {
        copyLink(file, dst.getPath(), failures);
      } else if (file.isDirectory()) {
        createDirectory(file, dst, failures);
      } else {
        copyFile(file, dst.getPath(), failures);
      }
      notifyListener(file, dst.getPath());
    }
  }

  private void copyLink(FileInfo src, String dst, Collection<Failure> failures) {
    try {
      String target = readlink(src.path());
      symlink(target, dst);
      setLastModifiedDate(src, dst);
    } catch (IOException e) {
      failures.add(Failure.create(src.path(), e));
    }
  }

  private void createDirectory(
      FileInfo src, File dst, Collection<Failure> failures) {
    try {
      forceMkdir(dst);
      setLastModifiedDate(src, dst.getPath());
    } catch (IOException e) {
      failures.add(Failure.create(src.path(), e));
    }
  }

  private void copyFile(FileInfo src, String dst, Collection<Failure> failures)
      throws InterruptedException {
    checkInterrupt();

    FileInputStream fis = null;
    FileOutputStream fos = null;
    try {

      fis = new FileInputStream(src.path());
      fos = new FileOutputStream(dst);
      FileChannel input = fis.getChannel();
      FileChannel output = fos.getChannel();
      long size = input.size();
      long pos = 0;
      while (pos < size) {
        long count = (size - pos) > BUFFER_SIZE ? BUFFER_SIZE : size - pos;
        pos += output.transferFrom(input, pos, count);
        notifyListener(src, dst);
      }

    } catch (IOException e) {
      if (!new File(dst).delete()) {
        logger.warn(e, "Failed to delete file on exception %s", dst);
      }
      if (e instanceof ClosedByInterruptException) {
        throw new InterruptedException();
      } else {
        failures.add(Failure.create(src.path(), e));
      }

    } finally {
      closeQuietly(fos);
      closeQuietly(fis);
    }

    setLastModifiedDate(src, dst);
  }

  private void setLastModifiedDate(FileInfo src, String dst) {
    File dstFile = new File(dst);
    File srcFile = new File(src.path());
    if (!dstFile.setLastModified(srcFile.lastModified())) {
      logger.warn("Failed to set last modified date on %s", dst);
    }
  }

  private void notifyListener(FileInfo src, String dst) {
    try {
      listener.onCopy(src, FileInfo.get(dst));
    } catch (IOException e) {
      logger.warn(e);
    }
  }

  public static interface Listener {
    void onCopy(FileInfo src, FileInfo dst);
  }
}
