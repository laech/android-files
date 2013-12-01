package l.files.service;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static l.files.common.io.Files.getNonExistentDestinationFile;
import static l.files.common.io.Files.isAncestorOrSelf;

final class FilesCutter {

  private final Cancellable listener;
  private final Set<File> sources;
  private final File destination;

  FilesCutter(Cancellable listener, Set<File> sources, File destination) {
    this.listener = listener;
    this.sources = sources;
    this.destination = destination;
  }

  Set<File> executeAndGetFailures() throws IOException {
    Set<File> failures = newHashSet();
    for (File from : sources) {
      if (listener.isCancelled()) {
        break;
      }
      // TODO
      if (isAncestorOrSelf(destination, from)) {
        throw new IOException("Cannot move directory " + from +
            " into its own sub directory " + destination);
      }
      File to = getNonExistentDestinationFile(from, destination);
      if (!from.renameTo(to)) {
        failures.add(from);
      }
    }

    return failures;
  }
}
