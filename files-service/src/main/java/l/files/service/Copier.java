package l.files.service;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static l.files.common.io.Files.replace;
import static l.files.service.BuildConfig.DEBUG;
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
  private static final long FILE_COPY_BUFFER_SIZE = 1024 * 4;

  private final Listener listener;
  private final long bytesTotal;
  private int remaining;
  private long bytesCopied;

  Copier(Listener listener, Set<File> sources, File destination, int remaining, long length) {
    super(listener, sources, destination);
    this.listener = checkNotNull(listener, "listener");
    this.remaining = remaining;
    this.bytesTotal = length;
  }

  @Override protected void paste(File from, File to) throws IOException {
    Queue<File> queue = newLinkedList(singletonList(from));
    while (!queue.isEmpty()) {
      if (listener.isCancelled()) {
        break;
      }

      File file = queue.poll();
      if (isSymlink(file)) {
        continue;
      }

      File dst = replace(file, from, to);
      if (file.isDirectory()) {
        queue.addAll(getChildren(file));
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

  private List<File> getChildren(File dir) throws RestrictedException {
    File[] children = dir.listFiles();
    if (children == null) {
      throw new RestrictedException(dir);
    }
    return asList(children);
  }

  private void copyFile(File srcFile, File destFile) throws IOException {
    if (listener.isCancelled()) {
      return;
    }
    if (!srcFile.exists()) {
      return;
    }
    File parentFile = destFile.getParentFile();
    if (!parentFile.mkdirs() && !parentFile.isDirectory()) {
      throw new IOException("Destination '" + parentFile + "' directory cannot be created");
    }
    if (destFile.exists() && !destFile.canWrite()) {
      throw new IOException("Destination '" + destFile + "' exists but is read-only");
    }
    doCopyFile(srcFile, destFile);
  }

  private void doCopyFile(File srcFile, File destFile) throws IOException {
    if (listener.isCancelled()) {
      return;
    }

    FileInputStream fis = null;
    FileOutputStream fos = null;
    FileChannel input = null;
    FileChannel output = null;
    try {
      fis = new FileInputStream(srcFile);
      fos = new FileOutputStream(destFile);
      input = fis.getChannel();
      output = fos.getChannel();
      final long size = input.size();
      long pos = 0;
      long count;
      while (pos < size) {
        if (listener.isCancelled()) {
          break;
        }
        count = (size - pos) > FILE_COPY_BUFFER_SIZE
            ? FILE_COPY_BUFFER_SIZE
            : size - pos;
        long copied = output.transferFrom(input, pos, count);
        pos += copied;

        bytesCopied += copied;
        listener.onCopied(remaining, bytesCopied, bytesTotal);
      }
    } catch (ClosedByInterruptException e) {
      if (!destFile.delete() && DEBUG) {
        Log.d(TAG, "Failed to delete file on cancel: " + destFile);
      }
    } finally {
      closeQuietly(output);
      closeQuietly(fos);
      closeQuietly(input);
      closeQuietly(fis);
    }

    if (srcFile.length() != destFile.length()) {
      if (listener.isCancelled()) {
        if (!destFile.delete() && DEBUG) {
          Log.d(TAG, "Failed to delete file on cancel: " + destFile);
        }
        return;
      } else {
        throw new IOException("Failed to copy full contents from '"
            + srcFile + "' to '" + destFile + "'");
      }
    }
    if (!destFile.setLastModified(srcFile.lastModified())) {
      if (DEBUG) {
        Log.d(TAG, "Failed to set last modified date on " + destFile);
      }
    }

    remaining--;
    listener.onCopied(remaining, bytesCopied, bytesTotal);
  }

  static interface Listener extends Cancellable {
    void onCopied(int remaining, long bytesCopied, long bytesTotal);
  }
}
