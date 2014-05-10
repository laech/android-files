package l.files.io.file.operations;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Counts the number of files and their sizes recursively. Directories
 * themselves are excluded but their files will be included.
 */
public final class Count extends Traverser<Count.Result> {

  private final Listener listener;

  private int count;
  private long length;

  public Count(Cancellable cancellable, Iterable<File> files, Listener listener) {
    super(cancellable, files);
    this.listener = checkNotNull(listener, "listener");
  }

  @Override protected void onFile(File file) {
    count++;
    length += file.length();
    listener.onFileCounted(count, length);
  }

  @Override protected Result getResult() {
    return new Result(count, length);
  }

  public static final class Result {
    public final int count;
    public final long length;

    Result(int count, long length) {
      this.count = count;
      this.length = length;
    }
  }

  public static interface Listener {
    void onFileCounted(int count, long length);
  }
}
