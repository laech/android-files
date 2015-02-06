package l.files.operations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;

import l.files.fs.Path;
import l.files.fs.PathEntry;
import l.files.fs.ResourceStatus;
import l.files.fs.local.Files;
import l.files.fs.local.LocalFileVisitor;
import l.files.fs.local.LocalPath;
import l.files.fs.local.LocalResourceStatus;
import l.files.logging.Logger;

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

  public Copy(Iterable<? extends Path> sources, Path dstDir) {
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

  @Override void paste(Path from, Path to, FailureRecorder listener)
      throws InterruptedException {

    File oldRoot = new File(from.getUri());
    File newRoot = new File(to.getUri());

    try {
      for (PathEntry entry : LocalFileVisitor.get().preOrderTraversal(from)) {
        checkInterrupt();

        LocalResourceStatus file;
        try {
          file = LocalResourceStatus.stat(entry.getPath(), false);
        } catch (IOException e) {
          listener.onFailure(entry.getPath(), e);
          continue;
        }

        File dst = Files.replace(LocalPath.check(entry.getPath()).getFile(), oldRoot, newRoot);
        if (file.getIsSymbolicLink()) {
          copyLink(file, LocalPath.of(dst), listener);
        } else if (file.getIsDirectory()) {
          createDirectory(file, LocalPath.of(dst), listener);
        } else {
          copyFile(file, LocalPath.of(dst.getPath()), listener);
        }
      }
    } catch (IOException e) {
      listener.onFailure(from, e);
    }
  }

  private void copyLink(ResourceStatus src, Path dst, FailureRecorder listener) {
    try {
      String target = readlink(new File(src.getPath().getUri()).getPath()); // TODO fix this
      symlink(target, new File(dst.getUri()).getPath()); // TODO fix this
      copiedByteCount += src.getSize();
      copiedItemCount++;
      setLastModifiedDate(src, dst);
    } catch (IOException e) {
      listener.onFailure(src.getPath(), e);
    }
  }

  private void createDirectory(ResourceStatus src, Path dst, FailureRecorder listener) {
    try {
      forceMkdir(new File(dst.getUri())); // TODO fix this
      copiedByteCount += src.getSize();
      copiedItemCount++;
      setLastModifiedDate(src, dst);
    } catch (IOException e) {
      listener.onFailure(src.getPath(), e);
    }
  }

  private void copyFile(ResourceStatus src, Path dst, FailureRecorder listener)
      throws InterruptedException {
    checkInterrupt();

    FileInputStream fis = null;
    FileOutputStream fos = null;
    try {

      fis = new FileInputStream(new File(src.getPath().getUri()));// TODO fix this
      fos = new FileOutputStream(new File(dst.getUri())); // TODO fix this
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
      if (!new File(dst.getUri()).delete()) { // TODO fix this
        logger.warn(e, "Failed to delete path on exception %s", dst);
      }
      if (e instanceof ClosedByInterruptException) {
        throw new InterruptedException();
      } else {
        listener.onFailure(src.getPath(), e);
      }

    } finally {
      closeQuietly(fos);
      closeQuietly(fis);
    }

    setLastModifiedDate(src, dst);
  }

  private void setLastModifiedDate(ResourceStatus src, Path dst) {
    File dstFile = new File(dst.getUri()); // TODO fix this
    //noinspection StatementWithEmptyBody
    if (!dstFile.setLastModified(src.getLastModifiedTime())) {
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
