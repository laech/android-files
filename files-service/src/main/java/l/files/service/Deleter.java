package l.files.service;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

final class Deleter extends Traverser<Void> implements Callable<Void> {

  private static final String TAG = Deleter.class.getSimpleName();

  private final Set<File> files;
  private final Listener listener;

  private final int total;
  private int remaining;

  Deleter(Listener listener, Set<File> files, int remaining) {
    super(listener);
    this.files = files;
    this.listener = listener;
    this.total = remaining;
    this.remaining = remaining;
  }

  @Override public Void call() throws IOException {
    for (File file : files) {
      if (file.isDirectory()) {
        walk(file, null);
      } else if (file.isFile()) {
        deleteFile(file);
      }
      // else file doesn't exist, so skip
    }
    return null;
  }

  @Override protected void handleFile(
      File file, int depth, Collection<Void> results) throws IOException {
    super.handleFile(file, depth, results);
    deleteFile(file);
  }

  private void deleteFile(File file) {
    if (file.delete()) {
      remaining--;
      listener.onFileDeleted(total, remaining);
    } else {
      // TODO
      Log.w(TAG, "Failed to delete file " + file.getAbsolutePath());
    }
  }

  @Override protected void handleDirectoryEnd(
      File directory, int depth, Collection<Void> results) throws IOException {
    super.handleDirectoryEnd(directory, depth, results);
    if (!directory.delete()) {
      Log.w(TAG, "Failed to delete directory " + directory.getAbsolutePath());
    }
  }

  static interface Listener extends Cancellable {
    void onFileDeleted(int total, int remaining);
  }
}
