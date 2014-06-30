package l.files.io.file.operations;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import l.files.io.file.Files;
import l.files.logging.Logger;

import static com.google.common.base.Preconditions.checkNotNull;
import static l.files.io.file.Files.isAncestorOrSelf;
import static l.files.io.file.operations.FileOperations.checkInterrupt;

public abstract class Paste extends AbstractOperation {

    private static final Logger logger = Logger.get(Paste.class);

    private final String dstPath;

    public Paste(Iterable<String> sources, String dstPath) {
        super(sources);
        this.dstPath = checkNotNull(dstPath, "dstPath");
    }

    @Override
    protected void process(String from, List<Failure> failures) throws InterruptedException {
        checkInterrupt();

        // TODO clean up the "File" usage

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
            failures.add(Failure.create(from, e));
            logger.warn(e);
            return;
        }

        File to = Files.getNonExistentDestinationFile(fromFile, destinationFile);
        paste(from, to.getPath(), failures);
    }

    /**
     * Pastes the source to the destination. If {@code from} is a file, write its
     * content into {@code to}. If {@code from} is a directory, paste its content
     * into {@code to}.
     */
    protected abstract void paste(String from, String to, Collection<Failure> failures)
            throws InterruptedException;

}
