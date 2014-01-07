package l.files.service;

import java.io.File;

final class Counter extends Traverser<Counter.Result> {

  private final Listener listener;

  private int count;
  private long length;

  Counter(Listener listener, Iterable<File> files) {
    super(listener, files);
    this.listener = listener;
  }

  @Override protected void onFile(File file) {
    count++;
    length += file.length();
    listener.onFileCounted(count, length);
  }

  @Override protected Result getResult() {
    return new Result(count, length);
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
