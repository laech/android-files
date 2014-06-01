package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import l.files.io.file.DirectoryTreeTraverser;
import l.files.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.Files.remove;
import static l.files.io.file.operations.FileException.throwIfNotEmpty;
import static l.files.io.file.operations.FileOperations.checkInterrupt;

public final class Delete implements FileOperation<Void> {

  private static final Logger logger = Logger.get(Delete.class);

  private final Listener listener;
  private final Iterable<String> paths;

  public Delete(Listener listener, Iterable<String> paths) {
    this.listener = checkNotNull(listener, "listener");
    this.paths = ImmutableSet.copyOf(paths);
  }

  @Override public Void call() throws InterruptedException {
    List<Failure> failures = new ArrayList<>(0);
    for (String path : paths) {
      delete(path, failures);
    }
    throwIfNotEmpty(failures);
    return null;
  }

  private void delete(String path, List<Failure> failures)
      throws InterruptedException {

    for (String entry : DirectoryTreeTraverser.get().postOrderTraversal(path)) {
      try {
        delete(entry);
        listener.onDelete(entry);
      } catch (IOException e) {
        failures.add(Failure.create(entry, e));
        logger.warn(e);
      }
    }
  }

  private void delete(String path) throws IOException, InterruptedException {
    checkInterrupt();
    remove(path);
  }

  public static interface Listener {
    void onDelete(String path);
  }
}