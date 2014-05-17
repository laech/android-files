package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import l.files.io.file.FileInfo;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableList;
import static l.files.io.file.operations.FileTraverser.postOrderTraversal;

public final class Delete implements FileOperation {

  private final Listener listener;
  private final Iterable<String> paths;

  public Delete(Listener listener, Iterable<String> paths) {
    this.listener = checkNotNull(listener, "listener");
    this.paths = ImmutableSet.copyOf(paths);
  }

  @Override public List<Failure> call() {
    List<Failure> failures = new ArrayList<>(0);
    for (String path : paths) {
      delete(path, failures);
    }
    return unmodifiableList(failures);
  }

  private void delete(String path, List<Failure> failures) {
    for (FileInfo info : postOrderTraversal(path)) {
      try {
        delete(info);
        listener.onDelete(info);
      } catch (IOException e) {
        failures.add(Failure.create(info.path(), e));
      }
    }
  }

  private void delete(FileInfo info) throws IOException {
    if (currentThread().isInterrupted()) {
      throw new CancellationException();
    }
    File file = new File(info.path());
    if (!file.delete() && file.exists()) {
      throw new NoWriteException(file);
    }
  }

  public static interface Listener {
    void onDelete(FileInfo file);
  }
}
