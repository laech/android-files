package l.files.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

// TODO avoid recursion on deep directories
final class FilesCounter extends Traverser<FilesCounter.Result> {

  private final Set<File> files;
  private final Listener listener;

  private int count;
  private long length;

  FilesCounter(Listener listener, Set<File> files) {
    super(listener);
    this.files = files;
    this.listener = listener;
  }

  Result execute() throws IOException {
    for (File file : files) {
      if (file.isDirectory()) {
        walk(file, null);
      } else {
        countFile(file);
      }
    }
    return new Result(count, length);
  }

  @Override protected void handleFile(
      File file, int depth, Collection<Result> results) throws IOException {
    super.handleFile(file, depth, results);
    countFile(file);
  }

  private void countFile(File file) {
    count++;
    length += file.length();
    listener.onFileCounted(count, length);
  }

  static final class Result {
    final int count;
    final long length;

    Result(int count, long length) {
      this.count = count;
      this.length = length;
    }
  }

  static interface Listener extends Cancellable {
    void onFileCounted(int count, long length);
  }
}
