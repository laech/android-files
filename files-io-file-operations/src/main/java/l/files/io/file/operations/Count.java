package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import l.files.io.file.DirectoryTreeTraverser;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.DirectoryTreeTraverser.Entry;
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
    Entry root = Entry.create(path);
    for (Entry entry : DirectoryTreeTraverser.get().breadthFirstTraversal(root)) {
      checkInterrupt();
      listener.onCount(entry.path());
    }
  }

  public static interface Listener {
    void onCount(String path);
  }
}
