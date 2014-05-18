package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.concurrent.CancellationException;

import l.files.io.file.FileInfo;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static l.files.io.file.operations.FileTraverser.breadthFirstTraversal;

public final class Count implements FileOperation {

  private final Listener listener;
  private final Iterable<String> paths;

  public Count(Listener listener, Iterable<String> paths) {
    this.listener = checkNotNull(listener, "listener");
    this.paths = ImmutableSet.copyOf(paths);
  }

  @Override public List<Failure> call() {
    for (String path : paths) {
      count(path);
    }
    return emptyList();
  }

  private void count(String path) {
    for (FileInfo file : breadthFirstTraversal(path)) {
      if (currentThread().isInterrupted()) {
        throw new CancellationException();
      }
      listener.onCounted(file);
    }
  }

  public static interface Listener {
    void onCounted(FileInfo file);
  }
}
