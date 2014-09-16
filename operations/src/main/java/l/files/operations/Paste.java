package l.files.operations;

import java.io.File;
import java.io.IOException;

import l.files.io.file.Files;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.Files.isAncestorOrSelf;

abstract class Paste extends AbstractOperation {

  private final String dstPath;

  Paste(Iterable<String> sources, String dstPath) {
    super(sources);
    this.dstPath = checkNotNull(dstPath, "dstPath");
  }

  @Override void process(String from, FailureRecorder listener)
      throws InterruptedException {
    checkInterrupt();

    File destinationFile = new File(dstPath);
    File fromFile = new File(from);
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
    paste(from, to.getPath(), listener);
  }

  /**
   * Pastes the source to the destination. If {@code from} is a file, write its
   * content into {@code to}. If {@code from} is a directory, paste its content
   * into {@code to}.
   */
  abstract void paste(String from, String to, FailureRecorder listener)
      throws InterruptedException;

}
