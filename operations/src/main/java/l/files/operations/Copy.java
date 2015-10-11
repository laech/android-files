package l.files.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.File;
import l.files.fs.Stat;

import static l.files.fs.LinkOption.NOFOLLOW;

final class Copy extends Paste {

    private static final int BUFFER_SIZE = 1024 * 8;

    private final AtomicLong copiedByteCount = new AtomicLong();
    private final AtomicInteger copiedItemCount = new AtomicInteger();

    Copy(Collection<? extends File> sources, File destination) {
        super(sources, destination);
    }

    public int getCopiedItemCount() {
        return copiedItemCount.get();
    }

    public long getCopiedByteCount() {
        return copiedByteCount.get();
    }

    @Override
    void paste(final File from, final File to) throws IOException {

        from.traverse(NOFOLLOW, new OperationVisitor() {

            @Override
            public Result onPreVisit(File src) throws IOException {
                copyItems(src, from, to);
                return super.onPreVisit(src);
            }

            @Override
            public Result onPostVisit(File src) throws IOException {
                updateDirectoryTimestamps(src, from, to);
                return super.onPostVisit(src);
            }

        });

    }

    private void copyItems(
            File src,
            File fromParent,
            File toParent) throws IOException {

        Stat stat = src.stat(NOFOLLOW);
        File dst = src.resolveParent(fromParent, toParent);

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
            File src,
            File fromParent,
            File toParent) throws IOException {

        Stat stat = src.stat(NOFOLLOW);
        File dst = src.resolveParent(fromParent, toParent);
        if (stat.isDirectory()) {
            setTimes(stat, dst);
        }

    }

    private void copyLink(File src, Stat stat, File dst) throws IOException {
        dst.createLink(src.readLink());
        copiedByteCount.addAndGet(stat.size());
        copiedItemCount.incrementAndGet();
        setTimes(stat, dst);
    }

    private void createDirectory(Stat stat, File dst) throws IOException {
        dst.createDir();
        copiedByteCount.addAndGet(stat.size());
        copiedItemCount.incrementAndGet();
    }

   private void copyFile(File src, Stat stat, File dst) throws IOException {
        if (isInterrupted()) {
            return;
        }

        try (InputStream source = src.newInputStream();
             OutputStream sink = dst.newOutputStream()) {

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

            setTimes(stat, dst);

        } catch (IOException e) {

            try {
                dst.delete();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (!(e instanceof ClosedByInterruptException) &&
                    !(e instanceof InterruptedIOException)) {
                throw e;
            }
        }
    }

    private void setTimes(Stat src, File dst) {
        try {
            dst.setLastModifiedTime(NOFOLLOW, src.lastModifiedTime());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dst.setLastAccessedTime(NOFOLLOW, src.lastAccessedTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
