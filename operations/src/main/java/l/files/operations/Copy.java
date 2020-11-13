package l.files.operations;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

final class Copy extends Paste {

    // TODO copy file/directory permissions

    private static final int BUFFER_SIZE = 1024 * 8;

    private final AtomicLong copiedByteCount = new AtomicLong();
    private final AtomicInteger copiedItemCount = new AtomicInteger();

    Copy(Collection<? extends Path> sourcePaths, Path destinationDir) {
        super(sourcePaths, destinationDir);
    }

    int getCopiedItemCount() {
        return copiedItemCount.get();
    }

    long getCopiedByteCount() {
        return copiedByteCount.get();
    }

    @Override
    void paste(Path sourcePath, Path destinationPath)
        throws IOException {

        walkFileTree(sourcePath, new OperationVisitor() {
            @Override
            public FileVisitResult visitFile(
                Path file,
                BasicFileAttributes attrs
            ) throws IOException {
                copyItems(file, sourcePath, destinationPath);
                return super.visitFile(file, attrs);
            }

            @Override
            public FileVisitResult preVisitDirectory(
                Path dir,
                BasicFileAttributes attrs
            ) throws IOException {
                copyItems(dir, sourcePath, destinationPath);
                return super.preVisitDirectory(dir, attrs);
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException e)
                throws IOException {
                if (e == null) {
                    updateDirectoryLastModifiedTime(
                        dir,
                        sourcePath,
                        destinationPath
                    );
                }
                return super.postVisitDirectory(dir, e);
            }
        });
    }

    private void copyItems(
        Path sourcePath,
        Path sourceRoot,
        Path destinationRoot
    ) throws IOException {

        BasicFileAttributes sourceAttrs = readAttributes(
            sourcePath,
            BasicFileAttributes.class,
            NOFOLLOW_LINKS
        );

        Path destinationPath =
            rebase(sourcePath, sourceRoot, destinationRoot);

        if (sourceAttrs.isSymbolicLink()) {
            copyLink(sourcePath, sourceAttrs, destinationPath);

        } else if (sourceAttrs.isDirectory()) {
            createDirectory(sourceAttrs, destinationPath);

        } else if (sourceAttrs.isRegularFile()) {
            copyFile(sourcePath, sourceAttrs, destinationPath);

        } else {
            throw new IOException("Not file or directory");
        }

    }

    private Path rebase(
        Path sourcePath,
        Path sourceRoot,
        Path destinationRoot
    ) {
        return destinationRoot.resolve(sourceRoot.relativize(sourcePath));
    }

    private void updateDirectoryLastModifiedTime(
        Path source,
        Path sourceRoot,
        Path destinationRoot
    ) throws IOException {
        BasicFileAttributes sourceAttrs =
            readAttributes(source, BasicFileAttributes.class, NOFOLLOW_LINKS);
        Path destinationPath = rebase(source, sourceRoot, destinationRoot);
        updateLastModifiedTime(sourceAttrs, destinationPath);
    }

    private void copyLink(
        Path source,
        BasicFileAttributes sourceAttrs,
        Path destinationPath
    ) throws IOException {
        Path sourceLinkTarget = readSymbolicLink(source);
        Files.createSymbolicLink(destinationPath, sourceLinkTarget);
        copiedByteCount.addAndGet(sourceAttrs.size());
        copiedItemCount.incrementAndGet();
    }

    private void createDirectory(
        BasicFileAttributes sourceAttrs,
        Path destinationPath
    ) throws IOException {
        Files.createDirectory(destinationPath);
        copiedByteCount.addAndGet(sourceAttrs.size());
        copiedItemCount.incrementAndGet();
    }

    private void copyFile(
        Path sourcePath,
        BasicFileAttributes sourceAttrs,
        Path destinationPath
    ) throws IOException {

        if (isInterrupted()) {
            return;
        }

        try (InputStream source = newInputStream(sourcePath);
             OutputStream sink = newOutputStream(destinationPath)) {


            // TODO perform sync to disk
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = source.read(buf)) > 0) {

                if (isInterrupted()) {
                    throw new InterruptedIOException();
                }

                sink.write(buf, 0, n);
                copiedByteCount.addAndGet(n);
            }
            copiedItemCount.incrementAndGet();

            updateLastModifiedTime(sourceAttrs, destinationPath);

        } catch (IOException e) {

            try {
                delete(destinationPath);
            } catch (IOException ex) {
                Log.w(getClass().getSimpleName(),
                    "Failed to delete file on failure " + destinationPath, ex
                );
            }

            if (!(e instanceof ClosedByInterruptException) &&
                !(e instanceof InterruptedIOException)) {
                throw e;
            }

        }
    }

    private void updateLastModifiedTime(
        BasicFileAttributes sourceAttrs,
        Path destinationPath
    ) {
        try {
            setLastModifiedTime(
                destinationPath,
                sourceAttrs.lastModifiedTime()
            );
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(),
                "Failed to set last modified time " + destinationPath, e
            );
        }
    }

}
