package l.files.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;

import l.files.fs.local.DirectoryTreeTraverser;
import l.files.fs.local.LocalFileStatus;
import l.files.fs.local.Files;
import l.files.logging.Logger;

import static l.files.fs.local.DirectoryTreeTraverser.Entry;
import static l.files.fs.local.Files.readlink;
import static l.files.fs.local.Files.symlink;
import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.IOUtils.closeQuietly;

final class Copy extends Paste {

  private static final Logger logger = Logger.get(Copy.class);

  /* TODO check optimal size
   * Higher the buffer, faster the copy, but will affect the overall system
   * performance/responsiveness more.
   * Need to keep a good balance between speed and system performance.
   * This may be related:
   * http://stackoverflow.com/questions/4290679/why-high-io-rate-operations-slow-everything-on-linux
   */
  private static final long BUFFER_SIZE = 1024 * 4;

  private volatile long copiedByteCount;
  private volatile int copiedItemCount;

  public Copy(Iterable<String> sources, String dstDir) {
    super(sources, dstDir);
  }

  /**
   * Gets the number of bytes processed so far.
   */
  public long getCopiedByteCount() {
    return copiedByteCount;
  }

  /**
   * Gets the number of items processed so far.
   */
  public int getCopiedItemCount() {
    return copiedItemCount;
  }

  @Override void paste(String from, String to, FailureRecorder listener)
      throws InterruptedException {

    File oldRoot = new File(from);
    File newRoot = new File(to);
    Entry root = Entry.create(from);

    for (Entry entry : DirectoryTreeTraverser.get().preOrderTraversal(root)) {
      checkInterrupt();

      LocalFileStatus file;
      try {
        file = LocalFileStatus.read(entry.path());
      } catch (IOException e) {
        listener.onFailure(entry.path(), e);
        continue;
      }

      File dst = Files.replace(new File(entry.path()), oldRoot, newRoot);
      if (file.isSymbolicLink()) {
        copyLink(file, dst.getPath(), listener);
      } else if (file.isDirectory()) {
        createDirectory(file, dst, listener);
      } else {
        copyFile(file, dst.getPath(), listener);
      }
    }
  }

  private void copyLink(LocalFileStatus src, String dst, FailureRecorder listener) {
    try {
      String target = readlink(src.path());
      symlink(target, dst);
      copiedByteCount += src.size();
      copiedItemCount++;
      setLastModifiedDate(src, dst);
    } catch (IOException e) {
      listener.onFailure(src.path(), e);
    }
  }

  private void createDirectory(
      LocalFileStatus src, File dst, FailureRecorder listener) {
    try {
      forceMkdir(dst);
      copiedByteCount += src.size();
      copiedItemCount++;
      setLastModifiedDate(src, dst.getPath());
    } catch (IOException e) {
      listener.onFailure(src.path(), e);
    }
  }

  private void copyFile(LocalFileStatus src, String dst, FailureRecorder listener)
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
        long transferred = output.transferFrom(input, pos, count);
        pos += transferred;
        copiedByteCount += transferred;
      }
      copiedItemCount++;

    } catch (IOException e) {
      if (!new File(dst).delete()) {
        logger.warn(e, "Failed to delete file on exception %s", dst);
      }
      if (e instanceof ClosedByInterruptException) {
        throw new InterruptedException();
      } else {
        listener.onFailure(src.path(), e);
      }

    } finally {
      closeQuietly(fos);
      closeQuietly(fis);
    }

    setLastModifiedDate(src, dst);
  }

  private void setLastModifiedDate(LocalFileStatus src, String dst) {
    File dstFile = new File(dst);
    File srcFile = new File(src.path());
    //noinspection StatementWithEmptyBody
    if (!dstFile.setLastModified(srcFile.lastModified())) {
      /*
       * Setting last modified time currently fails, see:
       *
       * https://code.google.com/p/android/issues/detail?id=18624
       * https://code.google.com/p/android/issues/detail?id=34691
       * https://code.google.com/p/android/issues/detail?id=1992
       * https://code.google.com/p/android/issues/detail?id=1699
       * https://code.google.com/p/android/issues/detail?id=25460
       *
       * So comment this log out, since it always fails.
       */
      // logger.debug("Failed to set last modified date on %s", dst);
    }
  }
}
