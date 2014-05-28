package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import l.files.io.file.DirectoryTreeTraverser;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.operations.FileOperations.checkInterrupt;

public final class Count implements FileOperation<Void> {

  private final Listener listener;
  private final Iterable<String> paths;

  public Count(Listener listener, Iterable<String> paths) {
    this.listener = checkNotNull(listener, "listener");
    this.paths = ImmutableSet.copyOf(paths);
  }

  @Override public Void call() throws InterruptedException {
    for (String path : paths) {
      count(path);
    }
    return null;
  }

  private void count(String path) throws InterruptedException {
    for (String entry : DirectoryTreeTraverser.get().breadthFirstTraversal(path)) {
      checkInterrupt();
      listener.onCount(entry);
    }
  }

  public static interface Listener {
    void onCount(String path);
  }
}
