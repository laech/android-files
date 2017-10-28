package l.files.operations;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.LinkOption.NOFOLLOW;

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

        sourcePath.traverse(NOFOLLOW, new OperationVisitor() {

            @Override
            public Result onPreVisit(Path source) throws IOException {
                copyItems(source, sourcePath, destinationPath);
                return super.onPreVisit(source);
            }

            @Override
            public Result onPostVisit(Path source) throws IOException {
                updateDirectoryLastModifiedTime(source, sourcePath, destinationPath);
                return super.onPostVisit(source);
            }

        });

    }

    private void copyItems(
            Path sourcePath,
            Path sourceRoot,
            Path destinationRoot) throws IOException {

        Stat sourceStat = sourcePath.stat(NOFOLLOW);
        Path destinationPath = sourcePath.rebase(sourceRoot, destinationRoot);

        if (sourceStat.isSymbolicLink()) {
            copyLink(sourcePath, sourceStat, destinationPath);

        } else if (sourceStat.isDirectory()) {
            createDirectory(sourceStat, destinationPath);

        } else if (sourceStat.isRegularFile()) {
            copyFile(sourcePath, sourceStat, destinationPath);

        } else {
            throw new IOException("Not file or directory");
        }

    }

    private void updateDirectoryLastModifiedTime(
            Path source,
            Path sourceRoot,
            Path destinationRoot) throws IOException {

        Stat sourceStat = source.stat(NOFOLLOW);
        Path destinationPath = source.rebase(sourceRoot, destinationRoot);
        if (sourceStat.isDirectory()) {
            updateLastModifiedTime(sourceStat, destinationPath);
        }
    }

    private void copyLink(
            Path source,
            Stat sourceStat,
            Path destinationPath) throws IOException {

        Path sourceLinkTarget = source.readSymbolicLink();
        destinationPath.createSymbolicLink(sourceLinkTarget);
        copiedByteCount.addAndGet(sourceStat.size());
        copiedItemCount.incrementAndGet();
        updateLastModifiedTime(sourceStat, destinationPath);
    }

    private void createDirectory(
            Stat sourceStat,
            Path destinationPath) throws IOException {

        destinationPath.createDirectory();
        copiedByteCount.addAndGet(sourceStat.size());
        copiedItemCount.incrementAndGet();
    }

    private void copyFile(
            Path sourcePath,
            Stat sourceStat,
            Path destinationPath) throws IOException {

        if (isInterrupted()) {
            return;
        }

        try (InputStream source = sourcePath.newInputStream();
             OutputStream sink = destinationPath.newOutputStream(false)) {


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

            updateLastModifiedTime(sourceStat, destinationPath);

        } catch (IOException e) {

            try {
                destinationPath.delete();
            } catch (IOException ex) {
                Log.w(getClass().getSimpleName(),
                        "Failed to delete file on failure " + destinationPath, ex);
            }

            if (!(e instanceof ClosedByInterruptException) &&
                    !(e instanceof InterruptedIOException)) {
                throw e;
            }

        }
    }

    private void updateLastModifiedTime(
            Stat sourceStat,
            Path destinationPath) {
        try {
            Instant sourceLastModified = sourceStat.lastModifiedTime();
            destinationPath.setLastModifiedTime(NOFOLLOW, sourceLastModified);
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(),
                    "Failed to set last modified time " + destinationPath, e);
        }
    }

}
