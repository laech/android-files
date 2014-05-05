package l.files.service;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.Files.getNonExistentDestinationFile;
import static l.files.io.file.Files.isAncestorOrSelf;

abstract class Paster<T> implements Callable<T> {

  private final Cancellable cancellable;
  private final Set<File> sources;
  private final File destination;

  Paster(Cancellable cancellable, Iterable<File> sources, File destination) {
    this.cancellable = checkNotNull(cancellable, "cancellable");
    this.destination = checkNotNull(destination, "destination");
    this.sources = ImmutableSet.copyOf(checkNotNull(sources, "sources"));
  }

  @Override public final T call() throws IOException {
    for (File from : sources) {
      if (isCancelled()) {
        return null;
      }
      // if (!from.exists()) {
      // TODO Continue or not exists exception?
      // }
      if (!from.canRead()) {
        throw new NoReadException(from);
      }
      if (!destination.canWrite()) {
        throw new NoWriteException(destination);
      }
      if (isAncestorOrSelf(destination, from)) {
        throw new CannotPasteIntoSelfException("Cannot paste directory "
            + from + " into its own sub directory " + destination);
      }
      File to = getNonExistentDestinationFile(from, destination);
      paste(from, to);
    }

    return getResult();
  }

  /**
   * Pastes the source to the destination. If {@code from} is a file, write its
   * content into {@code to}. If {@code from} is a directory, paste its content
   * into {@code to}.
   */
  protected abstract void paste(File from, File to) throws IOException;

  /**
   * Returns the result of executing {@link #call()}.
   */
  protected T getResult() {return null;}

  protected final boolean isCancelled() {
    return cancellable.isCancelled();
  }
}
