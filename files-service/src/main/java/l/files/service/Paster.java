package l.files.service;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.Callable;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.common.io.Files.getNonExistentDestinationFile;
import static l.files.common.io.Files.isAncestorOrSelf;

abstract class Paster<T> implements Callable<T> {

  private final Cancellable listener;
  private final Set<File> sources;
  private final File destination;

  Paster(Cancellable listener, Set<File> sources, File destination) {
    this.listener = checkNotNull(listener, "listener");
    this.destination = checkNotNull(destination, "destination");
    this.sources = checkNotNull(sources, "sources");
  }

  @Override public final T call() throws IOException {
    for (File from : sources) {
      if (listener.isCancelled()) {
        break;
      }
      if (isAncestorOrSelf(destination, from)) {
        throw new IOException("Cannot paste directory " + from +
            " into its own sub directory " + destination);
      }
      File to = getNonExistentDestinationFile(from, destination);
      paste(from, to);
    }
    return getResult();
  }

  protected abstract void paste(File from, File to) throws IOException;

  protected T getResult() {return null;}
}
