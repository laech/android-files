package l.files.io.file.operations;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CancellationException;

import l.files.io.file.Files;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Thread.currentThread;
import static java.util.Collections.unmodifiableList;
import static l.files.io.file.Files.isAncestorOrSelf;

public abstract class Paste implements FileOperation {

  private final Iterable<String> sources;
  private final String destination;

  public Paste(Iterable<String> sources, String dstDir) {
    this.destination = checkNotNull(dstDir, "dstDir");
    this.sources = ImmutableSet.copyOf(checkNotNull(sources, "sources"));
  }

  @Override public final List<Failure> call() {
    List<Failure> failures = new ArrayList<>(0);
    for (String from : sources) {
      if (isCancelled()) {
        throw new CancellationException();
      }

      File destinationFile = new File(destination);
      File fromFile = new File(from);
      try {
        if (isAncestorOrSelf(destinationFile, fromFile)) {
          throw new CannotPasteIntoSelfException(
              "Cannot paste directory " + from +
                  " into its own sub directory " + destination
          );
        }
      } catch (IOException e) {
        failures.add(Failure.create(from, e));
        continue;
      }

      File to = Files.getNonExistentDestinationFile(fromFile, destinationFile);
      paste(from, to.getPath(), failures);
    }
    return unmodifiableList(failures);
  }

  /**
   * Pastes the source to the destination. If {@code from} is a file, write its
   * content into {@code to}. If {@code from} is a directory, paste its content
   * into {@code to}.
   */
  protected abstract void paste(String from, String to, Collection<Failure> failures);

  protected final boolean isCancelled() {
    return currentThread().isInterrupted();
  }
}
