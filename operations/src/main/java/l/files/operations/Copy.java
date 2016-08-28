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

import l.files.fs.Files;
import l.files.fs.Path;
import l.files.fs.Stat;

import static l.files.fs.Files.newInputStream;
import static l.files.fs.Files.newOutputStream;
import static l.files.fs.LinkOption.NOFOLLOW;

final class Copy extends Paste {

    private static final int BUFFER_SIZE = 1024 * 8;

    private final AtomicLong copiedByteCount = new AtomicLong();
    private final AtomicInteger copiedItemCount = new AtomicInteger();

    Copy(Collection<? extends Path> sources, Path destination) {
        super(sources, destination);
    }

    public int getCopiedItemCount() {
        return copiedItemCount.get();
    }

    public long getCopiedByteCount() {
        return copiedByteCount.get();
    }

    @Override
    void paste(final Path from, final Path to) throws IOException {

        l.files.fs.Files.traverse(from, NOFOLLOW, new OperationVisitor() {

            @Override
            public Result onPreVisit(Path src) throws IOException {
                copyItems(src, from, to);
                return super.onPreVisit(src);
            }

            @Override
            public Result onPostVisit(Path src) throws IOException {
                updateDirectoryTimestamps(src, from, to);
                return super.onPostVisit(src);
            }

        });

    }

    private void copyItems(
            Path src,
            Path fromParent,
            Path toParent) throws IOException {

        Stat stat = Files.stat(src, NOFOLLOW);
        Path dst = src.rebase(fromParent, toParent);

        if (stat.isSymbolicLink()) {
            copyLink(src, stat, dst);

        } else if (stat.isDirectory()) {
            createDirectory(stat, dst);

        } else if (stat.isRegularFile()) {
            copyFile(src, stat, dst);

        } else {
            throw new IOException("Not file or directory");
        }

    }

    private void updateDirectoryTimestamps(
            Path src,
            Path fromParent,
            Path toParent) throws IOException {

        Stat stat = Files.stat(src, NOFOLLOW);
        Path dst = src.rebase(fromParent, toParent);
        if (stat.isDirectory()) {
            setTimes(stat, dst);
        }

    }

    private void copyLink(Path src, Stat stat, Path dst) throws IOException {
        Files.createSymbolicLink(dst, Files.readSymbolicLink(src));
        copiedByteCount.addAndGet(stat.size());
        copiedItemCount.incrementAndGet();
        setTimes(stat, dst);
    }

    private void createDirectory(Stat stat, Path dst) throws IOException {
        Files.createDir(dst);
        copiedByteCount.addAndGet(stat.size());
        copiedItemCount.incrementAndGet();
    }

    private void copyFile(Path src, Stat stat, Path dst) throws IOException {
        if (isInterrupted()) {
            return;
        }

        InputStream source = newInputStream(src);
        try {

            OutputStream sink = newOutputStream(dst);
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

            setTimes(stat, dst);

        } catch (IOException e) {

            try {
                Files.delete(dst);
            } catch (IOException ex) {
                Log.w(getClass().getSimpleName(),
                        "Failed to delete file on failure " + dst, ex);
            }

            if (!(e instanceof ClosedByInterruptException) &&
                    !(e instanceof InterruptedIOException)) {
                throw e;
            }

        } finally {
            source.close();
        }
    }

    private void setTimes(Stat src, Path dst) {
        try {
            Files.setLastModifiedTime(dst, NOFOLLOW, src.lastModifiedTime());
        } catch (IOException e) {
            Log.w(getClass().getSimpleName(),
                    "Failed to set last modified time " + dst, e);
        }
    }

}
