package l.files.service;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

final class FilesDeleter extends Traverser<Void> {

  private static final String TAG = FilesDeleter.class.getSimpleName();

  private final Set<File> files;
  private final Listener listener;

  private final int total;
  private int remaining;

  FilesDeleter(Listener listener, Set<File> files, int remaining) {
    super(listener);
    this.files = files;
    this.listener = listener;
    this.total = remaining;
    this.remaining = remaining;
  }

  void execute() throws IOException {
    for (File file : files) {
      if (file.isDirectory()) {
        walk(file, null);
      } else if (file.isFile()) {
        deleteFile(file);
      }
      // else file doesn't exist, so skip
    }
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
