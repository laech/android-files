package l.files.operations;

import java.io.File;
import java.io.IOException;

import l.files.fs.Path;
import l.files.fs.local.Files;
import l.files.fs.local.LocalPath;

import static java.util.Objects.requireNonNull;
import static l.files.fs.local.Files.isAncestorOrSelf;

abstract class Paste extends AbstractOperation {

  private final Path dstPath;

  Paste(Iterable<? extends Path> sources, Path dstPath) {
    super(sources);
    this.dstPath = requireNonNull(dstPath, "dstPath");
  }

  @Override void process(Path from, FailureRecorder listener)
      throws InterruptedException {
    checkInterrupt();

    File destinationFile = new File(dstPath.getUri());
    File fromFile = new File(from.getUri());
    try {
      if (isAncestorOrSelf(destinationFile, fromFile)) {
        throw new CannotPasteIntoSelfException(
            "Cannot paste directory " + from +
                " into its own sub directory " + dstPath
        );
      }
    } catch (IOException e) {
      listener.onFailure(from, e);
      return;
    }

    File to = Files.getNonExistentDestinationFile(fromFile, destinationFile);
    paste(from, LocalPath.of(to), listener);
  }

  /**
   * Pastes the source to the destination. If {@code from} is a file, write its
   * content into {@code to}. If {@code from} is a directory, paste its content
   * into {@code to}.
   */
  abstract void paste(Path from, Path to, FailureRecorder listener)
      throws InterruptedException;

}
