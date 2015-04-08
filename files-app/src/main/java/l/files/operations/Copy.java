package l.files.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.logging.Logger;

import static l.files.fs.Resource.Stream;
import static l.files.fs.Resource.TraversalOrder.PRE_ORDER;

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
    void paste(Resource from, Resource to, FailureRecorder listener) throws InterruptedException {
        try (Stream resources = traverse(from, PRE_ORDER, listener)) {

            for (Resource resource : resources) {
                checkInterrupt();

                ResourceStatus status;
                try {
                    status = resource.readStatus(false);
                } catch (IOException e) {
                    listener.onFailure(resource, e);
                    continue;
                }

                Resource dst = resource.resolveParent(from, to);

                if (status.isSymbolicLink()) {
                    copyLink(status, dst, listener);

                } else if (status.isDirectory()) {
                    createDirectory(status, dst, listener);

                } else if (status.isRegularFile()) {
                    copyFile(status, dst, listener);

                } else {
                    listener.onFailure(resource, new IOException("Not a file or directory"));
                }
            }

        } catch (IOException e) {
            listener.onFailure(from, e);
        }

    }

    private void copyLink(ResourceStatus src, Resource dst, FailureRecorder listener) {
        try {
            dst.createSymbolicLink(src.getResource().readSymbolicLink());
            copiedByteCount.addAndGet(src.getSize());
            copiedItemCount.incrementAndGet();
            setLastModifiedDate(src, dst);
        } catch (IOException e) {
            listener.onFailure(src.getResource(), e);
        }
    }

    private void createDirectory(ResourceStatus src, Resource dst, FailureRecorder listener) {
        try {
            dst.createDirectory();
            copiedByteCount.addAndGet(src.getSize());
            copiedItemCount.incrementAndGet();
            setLastModifiedDate(src, dst);
        } catch (IOException e) {
            listener.onFailure(src.getResource(), e);
        }
    }

    private void copyFile(ResourceStatus src, Resource dst, FailureRecorder listener) throws InterruptedException {
        checkInterrupt();

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
                dst.getResource().delete();
            } catch (IOException ex) {
                logger.warn(ex, "Failed to delete path on exception %s", dst);
            }

            if (e instanceof ClosedByInterruptException) {
                throw new InterruptedException();
            } else {
                listener.onFailure(src.getResource(), e);
            }
        }

        setLastModifiedDate(src, dst);
    }

    private void setLastModifiedDate(ResourceStatus src, Resource dst) {
        try {
            dst.setLastModifiedTime(src.getLastModifiedTime());
        } catch (IOException e) {
            /*
             * Setting last modified time currently fails, see:
             *
             * https://code.google.com/p/android/issues/detail?id=18624
             * https://code.google.com/p/android/issues/detail?id=34691
             * https://code.google.com/p/android/issues/detail?id=1992
             * https://code.google.com/p/android/issues/detail?id=1699
             * https://code.google.com/p/android/issues/detail?id=25460
             * So comment this log out, since it always fails.
             */
            // logger.debug(e, "Failed to set last modified date on %s", dst);
        }
    }

}
