package l.files.service;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newLinkedList;

final class Deleter extends Traverser<Void> {

  private final List<File> directories;
  private final Listener listener;
  private final int total;
  private int remaining;

  Deleter(
      Cancellable cancellable,
      Iterable<File> files,
      Listener listener,
      int remaining) {
    super(cancellable, files);
    this.listener = checkNotNull(listener, "listener");
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
      throw new NoWriteException(file.getParentFile());
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
      if (!directory.delete() && directory.exists()) {
        throw new NoWriteException(directory.getParentFile());
      }
      it.remove();
    }
  }

  static interface Listener {
    void onFileDeleted(int total, int remaining);
  }
}
