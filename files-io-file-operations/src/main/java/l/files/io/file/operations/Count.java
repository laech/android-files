package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.concurrent.CancellationException;

import l.files.io.file.FileInfo;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.currentThread;

public final class Count implements FileOperation {

  private final Listener listener;
  private final Iterable<String> paths;

  public Count(Listener listener, Iterable<String> paths) {
    this.listener = checkNotNull(listener, "listener");
    this.paths = ImmutableSet.copyOf(paths);
  }

  @Override public void run() {
    for (String path : paths) {
      count(path);
    }
  }

  private void count(String path) {
    FileInfo root;
    try {
      root = FileInfo.get(path);
    } catch (IOException e) {
      // No accessible or no longer exists, do not count
      return;
    }

    for (FileInfo file : FileTraverser.get().breadthFirstTraversal(root)) {
      if (currentThread().isInterrupted()) {
        throw new CancellationException();
      }
      listener.onCount(file);
    }
  }

  public static interface Listener {
    void onCount(FileInfo file);
  }
}
