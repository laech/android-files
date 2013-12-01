package l.files.service;

import org.apache.commons.io.DirectoryWalker;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import static org.apache.commons.io.FileUtils.isSymlink;

// TODO avoid recursive StackOverflowException
class Traverser<T> extends DirectoryWalker<T> {

  private final Cancellable cancellable;

  Traverser(Cancellable cancellable) {
    this.cancellable = cancellable;
  }

  protected boolean isCancelled() {
    return cancellable.isCancelled();
  }

  @Override protected boolean handleIsCancelled(
      File file, int depth, Collection<T> results) throws IOException {
    return isCancelled();
  }

  @Override protected boolean handleDirectory(
      File directory, int depth, Collection<T> results) throws IOException {
    return !isSymlink(directory);
  }

  @Override protected void handleRestricted(
      File directory, int depth, Collection<T> results) throws IOException {
    throw new RestrictedException(directory);
  }
}
