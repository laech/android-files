package l.files.operations;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.FileSystem;
import l.files.fs.Instant;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Copy extends Paste {

    private static final int BUFFER_SIZE = 1024 * 8;

    private final AtomicLong copiedByteCount = new AtomicLong();
    private final AtomicInteger copiedItemCount = new AtomicInteger();

    Copy(
            Map<Path, FileSystem> sourcePaths,
            FileSystem destinationFs,
            Path destinationDir
    ) {
        super(sourcePaths, destinationFs, destinationDir);
    }

    public int getCopiedItemCount() {
        return copiedItemCount.get();
    }

    public long getCopiedByteCount() {
        return copiedByteCount.get();
    }

    @Override
    void paste(
            final FileSystem sourceFs,
            final Path sourcePath,
            final FileSystem destinationFs,
            final Path destinationPath
    ) throws IOException {

        sourceFs.traverse(sourcePath, NOFOLLOW, new OperationVisitor() {

            @Override
            public Result onPreVisit(Path source) throws IOException {
                copyItems(sourceFs, source, sourcePath, destinationFs, destinationPath);
                return super.onPreVisit(source);
            }

            @Override
            public Result onPostVisit(Path source) throws IOException {
                updateDirectoryLastModifiedTime(
                        sourceFs, source, sourcePath, destinationFs, destinationPath);
                return super.onPostVisit(source);
            }

        });

    }

    private void copyItems(
            FileSystem sourceFs,
            Path sourcePath,
            Path sourceRoot,
            FileSystem destinationFs,
            Path destinationRoot
    ) throws IOException {

        Stat sourceStat = sourceFs.stat(sourcePath, NOFOLLOW);
        Path destinationPath = sourcePath.rebase(sourceRoot, destinationRoot);

        if (sourceStat.isSymbolicLink()) {
            copyLink(sourceFs, sourcePath, sourceStat, destinationFs, destinationPath);

        } else if (sourceStat.isDirectory()) {
            createDirectory(sourceStat, destinationFs, destinationPath);

        } else if (sourceStat.isRegularFile()) {
            copyFile(sourceFs, sourcePath, sourceStat, destinationFs, destinationPath);

        } else {
            throw new IOException("Not file or directory");
        }

    }

    private void updateDirectoryLastModifiedTime(
            FileSystem sourceFs,
            Path source,
            Path sourceRoot,
            FileSystem destinationFs,
            Path destinationRoot
    ) throws IOException {

        Stat sourceStat = sourceFs.stat(source, NOFOLLOW);
        Path destinationPath = source.rebase(sourceRoot, destinationRoot);
        if (sourceStat.isDirectory()) {
            updateLastModifiedTime(sourceStat, destinationFs, destinationPath);
        }
    }

    private void copyLink(
            FileSystem sourceFs,
            Path source,
            Stat sourceStat,
            FileSystem destinationFs,
            Path destinationPath
    ) throws IOException {

        Path sourceLinkTarget = sourceFs.readSymbolicLink(source);
        destinationFs.createSymbolicLink(destinationPath, sourceLinkTarget);
        copiedByteCount.addAndGet(sourceStat.size());
        copiedItemCount.incrementAndGet();
        updateLastModifiedTime(sourceStat, destinationFs, destinationPath);
    }

    private void createDirectory(
            Stat sourceStat,
            FileSystem destinationFs,
            Path destinationPath
    ) throws IOException {

        destinationFs.createDir(destinationPath);
        copiedByteCount.addAndGet(sourceStat.size());
        copiedItemCount.incrementAndGet();
    }

    private void copyFile(
            FileSystem sourceFs,
            Path sourcePath,
            Stat sourceStat,
            FileSystem destinationFs,
            Path destinationPath
    ) throws IOException {

        if (isInterrupted()) {
            return;
        }

        InputStream source = sourceFs.newInputStream(sourcePath);
        try {

            OutputStream sink = destinationFs.newOutputStream(destinationPath, false);
            try {
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
            } finally {
                sink.close();
            }
            copiedItemCount.incrementAndGet();

            updateLastModifiedTime(sourceStat, destinationFs, destinationPath);

        } catch (IOException e) {

            try {
                destinationFs.delete(destinationPath);
            } catch (IOException ex) {
                Log.w(getClass().getSimpleName(),
                        "Failed to delete file on failure " + destinationPath, ex);
            }

            if (!(e instanceof ClosedByInterruptException) &&
                    !(e instanceof InterruptedIOException)) {
                throw e;
            }

        } finally {
            source.close();
        }
    }

    private void updateLastModifiedTime(
            Stat sourceStat,
            FileSystem destinationFs,
            Path destinationPath
    ) {
        try {
            Instant sourceLastModified = sourceStat.lastModifiedTime();
            destinationFs.setLastModifiedTime(destinationPath, NOFOLLOW, sourceLastModified);
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(),
                    "Failed to set last modified time " + destinationPath, e);
        }
    }

}
