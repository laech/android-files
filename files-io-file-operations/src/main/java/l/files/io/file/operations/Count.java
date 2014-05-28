package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;

import l.files.io.file.FileInfo;
import l.files.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.operations.FileOperations.checkInterrupt;

public final class Count implements FileOperation<Void> {

  private static final Logger logger = Logger.get(Count.class);

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
    FileInfo root;
    try {
      root = FileInfo.get(path);
    } catch (IOException e) {
      // No accessible or no longer exists, do not count
      logger.warn(e);
      return;
    }

    for (FileInfo file : FileTraverser.get().breadthFirstTraversal(root)) {
      checkInterrupt();
      listener.onCount(file);
    }
  }

  public static interface Listener {
    void onCount(FileInfo file);
  }
}
