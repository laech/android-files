package l.files.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.fs.ResourceVisitor;
import l.files.logging.Logger;

import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.ResourceVisitor.Result.CONTINUE;
import static l.files.fs.ResourceVisitor.Result.TERMINATE;

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
        preOrderTraversal(from, new ResourceVisitor() {
            @Override
            public Result accept(Resource resource) throws IOException {
                if (isInterrupted()) {
                    return TERMINATE;
                }

                ResourceStatus status;
                try {
                    status = resource.readStatus(NOFOLLOW);
                } catch (IOException e) {
                    record(resource, e);
                    return CONTINUE;
                }

                Resource dst = resource.resolveParent(from, to);

                if (status.isSymbolicLink()) {
                    copyLink(status, dst);

                } else if (status.isDirectory()) {
                    createDirectory(status, dst);

                } else if (status.isRegularFile()) {
                    copyFile(status, dst);

                } else {
                    record(resource, new IOException("Not a file or directory"));
                }

                return CONTINUE;
            }
        });
    }

    private void copyLink(ResourceStatus src, Resource dst) {
        try {
            dst.createSymbolicLink(src.getResource().readSymbolicLink());
            copiedByteCount.addAndGet(src.getSize());
            copiedItemCount.incrementAndGet();
            setTimes(src, dst);
        } catch (IOException e) {
            record(src.getResource(), e);
        }
    }

    private void createDirectory(ResourceStatus src, Resource dst) {
        try {
            dst.createDirectory();
            copiedByteCount.addAndGet(src.getSize());
            copiedItemCount.incrementAndGet();
            setTimes(src, dst);
        } catch (IOException e) {
            record(src.getResource(), e);
        }
    }

    private void copyFile(ResourceStatus src, Resource dst) {
        if (isInterrupted()) {
            return;
        }

        try {

            try (InputStream source = src.getResource().openInputStream();
                 OutputStream sink = dst.openOutputStream()) {
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
                record(src.getResource(), e);
            }
        }

        setTimes(src, dst);
    }

    private void setTimes(ResourceStatus src, Resource dst) {
        try {
            dst.setModificationTime(src.getModificationTime());
        } catch (IOException e) {
            logger.debug(e, "Failed to set modification time on %s", dst);
        }
        try {
            dst.setAccessTime(src.getAccessTime());
        } catch (IOException e) {
            logger.debug(e, "Failed to set access time on %s", dst);
        }
    }

}
