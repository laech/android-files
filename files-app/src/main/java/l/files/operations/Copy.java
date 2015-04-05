package l.files.operations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ClosedByInterruptException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import kotlin.Function2;
import kotlin.Unit;
import l.files.fs.Path;
import l.files.fs.Resource;
import l.files.fs.ResourceStatus;
import l.files.logging.Logger;

import static l.files.fs.Resource.TraversalOrder.PRE_ORDER;

final class Copy extends Paste {

    private static final Logger logger = Logger.get(Copy.class);
    private static final int BUFFER_SIZE = 1024 * 8;

    private final AtomicLong copiedByteCount = new AtomicLong();
    private final AtomicInteger copiedItemCount = new AtomicInteger();

    Copy(Iterable<? extends Path> sources, Path dstPath) {
        super(sources, dstPath);
    }

    public int getCopiedItemCount() {
        return copiedItemCount.get();
    }

    public long getCopiedByteCount() {
        return copiedByteCount.get();
    }

    @Override
    void paste(Path from, Path to, final FailureRecorder listener) throws InterruptedException {

        Iterator<Resource> it;
        try {
            it = from.getResource().traverse(PRE_ORDER, new Function2<Resource, IOException, Unit>() {
                @Override
                public Unit invoke(Resource resource, IOException e) {
                    listener.onFailure(resource.getPath(), e);
                    return null;
                }
            }).iterator();
        } catch (IOException e) {
            listener.onFailure(from, e);
            return;
        }

        while (it.hasNext()) {
            checkInterrupt();

            Resource resource = it.next();
            Path path = resource.getPath();
            ResourceStatus status;
            try {
                status = resource.readStatus(false);
            } catch (IOException e) {
                listener.onFailure(path, e);
                continue;
            }

            Path dst = path.replace(from, to);

            if (status.getIsSymbolicLink()) {
                copyLink(status, dst, listener);

            } else if (status.getIsDirectory()) {
                createDirectory(status, dst, listener);

            } else if (status.getIsRegularFile()) {
                copyFile(status, dst, listener);

            } else {
                listener.onFailure(path, new IOException("Not a file or directory"));
            }
        }

    }

    private void copyLink(ResourceStatus src, Path dst, FailureRecorder listener) {
        try {
            dst.getResource().createSymbolicLink(src.getResource().readSymbolicLink());
            copiedByteCount.addAndGet(src.getSize());
            copiedItemCount.incrementAndGet();
            setLastModifiedDate(src, dst);
        } catch (IOException e) {
            listener.onFailure(src.getPath(), e);
        }
    }

    private void createDirectory(ResourceStatus src, Path dst, FailureRecorder listener) {
        try {
            dst.getResource().createDirectory();
            copiedByteCount.addAndGet(src.getSize());
            copiedItemCount.incrementAndGet();
            setLastModifiedDate(src, dst);
        } catch (IOException e) {
            listener.onFailure(src.getPath(), e);
        }
    }

    private void copyFile(ResourceStatus src, Path dst, FailureRecorder listener) throws InterruptedException {
        checkInterrupt();

        try {

            try (InputStream source = src.getResource().openInputStream();
                 OutputStream sink = dst.getResource().openOutputStream()) {
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
                listener.onFailure(src.getPath(), e);
            }
        }

        setLastModifiedDate(src, dst);
    }

    private void setLastModifiedDate(ResourceStatus src, Path dst) {
        try {
            dst.getResource().setLastModifiedTime(src.getLastModifiedTime());
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
