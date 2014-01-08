package l.files.service;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Collections.singletonList;
import static l.files.common.io.Files.replace;
import static l.files.service.BuildConfig.DEBUG;
import static l.files.service.Util.listDirectoryChildren;
import static org.apache.commons.io.FileUtils.isSymlink;
import static org.apache.commons.io.IOUtils.closeQuietly;

final class Copier extends Paster<Void> {

  /*
   * Setting last modified time currently fails, see:
   * https://code.google.com/p/android/issues/detail?id=18624
   * https://code.google.com/p/android/issues/detail?id=34691
   * https://code.google.com/p/android/issues/detail?id=1992
   * https://code.google.com/p/android/issues/detail?id=1699
   * https://code.google.com/p/android/issues/detail?id=25460
   */

  private static final String TAG = Copier.class.getSimpleName();

  /*
   * Higher the buffer, faster the copy, but will affect the overall system
   * performance/responsiveness more.
   * Need to keep a good balance between speed and system performance.
   * This may be related:
   * http://stackoverflow.com/questions/4290679/why-high-io-rate-operations-slow-everything-on-linux
   */
  private static final long BUFFER_SIZE = 1024 * 4;

  private final Listener listener;
  private final long bytesTotal;
  private int remaining;
  private long bytesCopied;

  Copier(
      Cancellable cancellable,
      Iterable<File> sources,
      File destination,
      Listener listener,
      int remaining,
      long length) {
    super(cancellable, sources, destination);
    this.listener = checkNotNull(listener, "listener");
    this.remaining = remaining;
    this.bytesTotal = length;
  }

  @Override protected void paste(File from, File to) throws IOException {
    Queue<File> queue = newLinkedList(singletonList(from));
    while (!queue.isEmpty()) {
      if (isCancelled()) {
        break;
      }

      File file = queue.poll();
      /*
       * Symlinks are not supported yet, there isn't a way to create symlinks
       * with the current Java API.
       */
      if (isSymlink(file)) {
        continue;
      }

      File dst = replace(file, from, to);
      if (file.isDirectory()) {
        queue.addAll(listDirectoryChildren(file));
        createDirectory(dst);
      } else {
        copyFile(file, dst);
      }
    }
  }

  private void createDirectory(File dir) {
    if (!dir.mkdirs() && !dir.exists()) {
      Log.w(TAG, "Failed to create directory: " + dir);
    }
  }

  private void copyFile(File srcFile, File dstFile) throws IOException {
    if (isCancelled()) return;
    if (!srcFile.exists()) return;

    File parentFile = dstFile.getParentFile();
    if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
      throw new IOException("Destination '" + parentFile + "' directory cannot be created");
    }
    if (dstFile.exists() && !dstFile.canWrite()) {
      throw new IOException("Destination '" + dstFile + "' exists but is read-only");
    }

    FileInputStream fis = null;
    FileOutputStream fos = null;
    FileChannel input = null;
    FileChannel output = null;
    try {
      fis = new FileInputStream(srcFile);
      fos = new FileOutputStream(dstFile);
      input = fis.getChannel();
      output = fos.getChannel();
      final long size = input.size();
      long pos = 0;
      while (pos < size) {
        if (isCancelled()) {
          break;
        }
        pos = onCopy(input, output, size, pos);
      }
    } catch (ClosedByInterruptException e) {
      if (!dstFile.delete() && DEBUG) {
        Log.d(TAG, "Failed to delete file on cancel: " + dstFile);
      }
    } finally {
      closeQuietly(output);
      closeQuietly(fos);
      closeQuietly(input);
      closeQuietly(fis);
    }

    onCopyFinished(srcFile, dstFile);
  }

  private long onCopy(FileChannel input, FileChannel output, long size, long pos) throws IOException {
    long count = (size - pos) > BUFFER_SIZE ? BUFFER_SIZE : size - pos;
    long copied = output.transferFrom(input, pos, count);
    long newPos = pos + copied;

    bytesCopied += copied;
    listener.onCopied(remaining, bytesCopied, bytesTotal);
    return newPos;
  }

  private void onCopyFinished(File srcFile, File dstFile) throws IOException {
    if (srcFile.length() != dstFile.length()) {
      if (isCancelled()) {
        if (!dstFile.delete() && DEBUG) {
          Log.d(TAG, "Failed to delete file on cancel: " + dstFile);
        }
        return;
      } else {
        throw new IOException("Failed to copy full contents from '"
            + srcFile + "' to '" + dstFile + "'");
      }
    }
    if (!dstFile.setLastModified(srcFile.lastModified())) {
      if (DEBUG) {
        Log.d(TAG, "Failed to set last modified date on " + dstFile);
      }
    }

    remaining--;
    listener.onCopied(remaining, bytesCopied, bytesTotal);
  }

  static interface Listener {
    void onCopied(int remaining, long bytesCopied, long bytesTotal);
  }
}
