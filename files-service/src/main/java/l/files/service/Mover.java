package l.files.service;

import java.io.File;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * Attempt to move files to their new destination, returns the ones that failed
 * to move.
 */
final class Mover extends Paster<Set<File>> {

  private final Set<File> failures;

  Mover(Cancellable listener, Set<File> sources, File destination) {
    super(listener, sources, destination);
    this.failures = newHashSet();
  }

  @Override protected Set<File> getResult() {
    return failures;
  }

  @Override protected void paste(File from, File to) {
    if (!from.renameTo(to)) {
      failures.add(from);
    }
  }
}
