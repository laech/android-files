package l.files.service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

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
      walk(file, null);
    }
    return new Result(count, length);
  }

  @Override protected void handleFile(
      File file, int depth, Collection<Result> results) throws IOException {
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
