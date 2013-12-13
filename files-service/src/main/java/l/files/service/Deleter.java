package l.files.service;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Lists.newLinkedList;

final class Deleter extends Traverser<Void> {

  private static final String TAG = Deleter.class.getSimpleName();

  private final List<File> directories;
  private final Listener listener;
  private final int total;
  private int remaining;

  Deleter(Listener listener, Set<File> files, int remaining) {
    super(listener, files);
    this.listener = listener;
    this.total = remaining;
    this.remaining = remaining;
    this.directories = newLinkedList();
  }

  @Override protected void onFile(File file) throws IOException {
    super.onFile(file);
    if (file.delete() || !file.exists()) {
      remaining--;
      listener.onFileDeleted(total, remaining);
    } else {
      // TODO
      Log.w(TAG, "Failed to delete file " + file);
    }
  }

  @Override protected void onDirectory(File dir) throws IOException {
    super.onDirectory(dir);
    directories.add(0, dir);
  }

  @Override protected void onFinish() throws IOException {
    super.onFinish();
    Iterator<File> it = directories.iterator();
    while (it.hasNext()) {
      File directory = it.next();
      if (!directory.delete()) {
        Log.w(TAG, "Failed to delete directory " + directory);
      }
      it.remove();
    }
  }

  static interface Listener extends Cancellable {
    void onFileDeleted(int total, int remaining);
  }
}
