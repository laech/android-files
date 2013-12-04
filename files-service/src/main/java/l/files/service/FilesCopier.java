package l.files.service;

import android.util.Log;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static l.files.common.io.Files.getNonExistentDestinationFile;
import static l.files.common.io.Files.isAncestorOrSelf;
import static l.files.common.io.Files.replace;
import static l.files.service.BuildConfig.DEBUG;
import static org.apache.commons.io.FileUtils.isSymlink;
import static org.apache.commons.io.IOUtils.closeQuietly;

final class FilesCopier implements Callable<Void> {

  private static final String TAG = FilesCopier.class.getSimpleName();

  /*
   * Higher the buffer, faster the copy, but will affect the overall system
   * performance/responsiveness more.
   * Need to keep a good balance between speed and system performance.
   * This may be related:
   * http://stackoverflow.com/questions/4290679/why-high-io-rate-operations-slow-everything-on-linux
   */
  private static final long FILE_COPY_BUFFER_SIZE = 1024 * 4;

  private final Set<File> sources;
  private final File destination;
  private final Listener listener;
  private final long bytesTotal;
  private int remaining;
  private long bytesCopied;

  FilesCopier(Listener listener, Set<File> sources, File destination, int remaining, long length) {
    this.listener = checkNotNull(listener, "listener");
    this.destination = checkNotNull(destination, "destination");
    this.sources = ImmutableSet.copyOf(checkNotNull(sources, "sources"));
    this.remaining = remaining;
    this.bytesTotal = length;
  }

  @Override public Void call() throws IOException {
    for (File from : sources) {
      if (listener.isCancelled()) {
        break;
      }
      if (isAncestorOrSelf(destination, from)) {
        throw new IOException("Cannot copy directory " + from +
            " into its own sub directory " + destination);
      }
      File to = getNonExistentDestinationFile(from, destination);
      copy(from, to);
    }
    return null;
  }

  private void copy(File from, File to) throws IOException {
    Queue<File> queue = newLinkedList(singletonList(from));
    while (!queue.isEmpty()) {
      if (listener.isCancelled()) {
        break;
      }

      File file = queue.poll();
      if (isSymlink(file)) {
        continue;
      }

      if (file.isDirectory()) {
        queue.addAll(getChildren(file));
      } else {
        copyFile(file, replace(file, from, to));
      }
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
