package l.files.operations;

import java.io.IOException;
import java.util.Map;

import l.files.fs.FileSystem;
import l.files.fs.Path;

import static l.files.base.Objects.requireNonNull;
import static l.files.operations.Files.getNonExistentDestinationFile;

abstract class Paste extends AbstractOperation {

    private final FileSystem destinationFs;
    private final Path destinationDir;

    Paste(
            Map<? extends Path, ? extends FileSystem> sourcePaths,
            FileSystem destinationFs,
            Path destinationDir
    ) {
        super(sourcePaths);
        this.destinationFs = requireNonNull(destinationFs, "destinationFs");
        this.destinationDir = requireNonNull(destinationDir, "destinationDir");
    }

    @Override
    void process(FileSystem sourceFs, Path sourcePath) throws InterruptedException {
        checkInterrupt();

        if (destinationDir.startsWith(sourcePath)) {
            throw new CannotPasteIntoSelfException(
                    "Cannot paste directory " + sourcePath +
                            " into its own sub directory " + destinationDir
            );
        }

        try {
            Path destinationPath = getNonExistentDestinationFile(sourcePath, destinationDir);
            paste(sourceFs, sourcePath, destinationFs, destinationPath);
        } catch (IOException e) {
            record(sourcePath, e);
        }
    }

    /**
     * Pastes the source to the destination. If {@code sourcePath} is a file, write
     * its content into {@code destinationPath}. If {@code sourcePath} is a directory,
     * paste its content into {@code destinationPath}.
     */
    abstract void paste(
            FileSystem sourceFs,
            Path sourcePath,
            FileSystem destinationFs,
            Path destinationPath
    ) throws IOException;

}
