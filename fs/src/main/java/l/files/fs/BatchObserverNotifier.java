package l.files.fs;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;

final class BatchObserverNotifier implements Observer, Closeable, Runnable {

    private static final ScheduledExecutorService service
            = newSingleThreadScheduledExecutor();

    private boolean selfChanged;
    private final Set<String> childrenChanged;
    private final BatchObserver batchObserver;

    private Closeable observation;
    private ScheduledFuture<?> checker;

    BatchObserverNotifier(BatchObserver batchObserver) {
        this.batchObserver = batchObserver;
        this.childrenChanged = new HashSet<>();
    }

    Closeable start(
            File file,
            LinkOption option,
            FileConsumer childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit) throws IOException {

        if (observation != null || checker != null) {
            throw new IllegalStateException();
        }

        try {

            observation = file.observe(option, this, childrenConsumer);
            checker = service.scheduleWithFixedDelay(
                    this, batchInterval, batchInterval, batchInternalUnit);

        } catch (Throwable e) {

            try {
                close();
            } catch (Exception ignored) {
            }
            throw e;

        }

        return this;
    }

    @Override
    public void onEvent(Event event, String child) {
        synchronized (this) {

            if (child == null) {
                selfChanged = true;
            } else {
                childrenChanged.add(child);
            }

        }
    }

    @Override
    public void run() {

        boolean snapshotSelfChanged;
        Set<String> snapshotChildrenChanged;

        synchronized (this) {

            snapshotSelfChanged = selfChanged;
            snapshotChildrenChanged = childrenChanged.isEmpty()
                    ? Collections.<String>emptySet()
                    : Collections.unmodifiableSet(new HashSet<>(childrenChanged));

            selfChanged = false;
            childrenChanged.clear();

        }

        if (snapshotSelfChanged || !snapshotChildrenChanged.isEmpty()) {
            batchObserver.onBatchEvent(
                    snapshotSelfChanged,
                    snapshotChildrenChanged
            );
        }

    }

    @Override
    public void close() throws IOException {
        if (checker != null) {
            checker.cancel(true);
        }
        if (observation != null) {
            observation.close();
        }
    }

}
