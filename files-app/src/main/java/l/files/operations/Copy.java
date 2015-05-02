package l.files.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.Visitor;
import l.files.logging.Logger;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

final class Copy extends Paste {

    private static final Logger logger = Logger.get(Copy.class);
    private static final int BUFFER_SIZE = 1024 * 8;

    private final AtomicLong copiedByteCount = new AtomicLong();
    private final AtomicInteger copiedItemCount = new AtomicInteger();

    Copy(Iterable<? extends Resource> sources, Resource destination) {
        super(sources, destination);
    }

    public int getCopiedItemCount() {
        return copiedItemCount.get();
    }

    public long getCopiedByteCount() {
        return copiedByteCount.get();
    }

    @Override
    void paste(final Resource from, final Resource to) throws IOException {
        preOrderTraversal(from, new Visitor() {
            @Override
            public Result accept(Resource src) throws IOException {
                if (isInterrupted()) {
                    return TERMINATE;
                }

                Stat stat;
                try {
                    stat = src.stat(NOFOLLOW);
                } catch (IOException e) {
                    record(src, e);
                    return CONTINUE;
                }

                Resource dst = src.resolveParent(from, to);

                if (stat.isSymbolicLink()) {
                    copyLink(src, stat, dst);

                } else if (stat.isDirectory()) {
                    createDirectory(src, stat, dst);

                } else if (stat.isRegularFile()) {
                    copyFile(src, stat, dst);

                } else {
                    record(src, new IOException("Not a file or directory"));
                }

                return CONTINUE;
            }
        });
    }

    private void copyLink(Resource src, Stat stat, Resource dst) {
        try {
            dst.createLink(src.readLink());
            copiedByteCount.addAndGet(stat.size());
            copiedItemCount.incrementAndGet();
            setTimes(stat, dst);
        } catch (IOException e) {
            record(src, e);
        }
    }

    private void createDirectory(Resource src, Stat stat, Resource dst) {
        try {
            dst.createDirectory();
            copiedByteCount.addAndGet(stat.size());
            copiedItemCount.incrementAndGet();
            setTimes(stat, dst);
        } catch (IOException e) {
            record(src, e);
        }
    }

    private void copyFile(Resource src, Stat stat, Resource dst) {
        if (isInterrupted()) {
            return;
        }

        try {

            try (InputStream source = src.input(NOFOLLOW);
                 OutputStream sink = dst.output(NOFOLLOW)) {
                byte[] buf = new byte[BUFFER_SIZE];
                int n;
                while ((n = source.read(buf)) > 0) {
                    sink.write(buf, 0, n);
                    copiedByteCount.addAndGet(n);
                }
                copiedItemCount.incrementAndGet();
            }

        } catch (IOException e) {
            try {
                dst.delete();
            } catch (IOException ex) {
                logger.warn(ex, "Failed to delete path on exception %s", dst);
            }

            if (e instanceof ClosedByInterruptException) {
                return;
            } else {
                record(src, e);
            }
        }

        setTimes(stat, dst);
    }

    private void setTimes(Stat src, Resource dst) {
        try {
            dst.setModificationTime(NOFOLLOW, src.modificationTime());
        } catch (IOException e) {
            logger.debug(e, "Failed to set modification time on %s", dst);
        }
        try {
            dst.setAccessTime(NOFOLLOW, src.accessTime());
        } catch (IOException e) {
            logger.debug(e, "Failed to set access time on %s", dst);
        }
    }

}
