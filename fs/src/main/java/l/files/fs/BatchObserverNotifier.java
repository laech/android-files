package l.files.fs;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

final class BatchObserverNotifier implements Observer, Observation, Runnable {

    private static final ScheduledExecutorService service = newScheduler();

    private static ScheduledExecutorService newScheduler() {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    private boolean selfChanged;
    private final Set<String> childrenChanged;
    private final BatchObserver batchObserver;

    private Observation observation;
    private ScheduledFuture<?> checker;

    BatchObserverNotifier(BatchObserver batchObserver) {
        this.batchObserver = batchObserver;
        this.childrenChanged = new HashSet<>();
    }

    Observation start(
            File file,
            LinkOption option,
            FileConsumer childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit) throws IOException, InterruptedException {

        if (observation != null) {
            throw new IllegalStateException();
        }

        try {

            observation = file.observe(option, this, childrenConsumer);
            if (!observation.isClosed()) {
                checker = service.scheduleWithFixedDelay(
                        this, batchInterval, batchInterval, batchInternalUnit);
            }

        } catch (Throwable e) {

            try {
                close();
            } catch (Exception sup) {
                e.addSuppressed(sup);
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
    public void onObserveFailed(String child) {
        batchObserver.onObserveFailed(child);
    }

    @Override
    public void onObserveRecovered(String child) {
        batchObserver.onObserveRecovered(child);
    }

    @Override
    public void onCancel() {
        if (checker != null) {
            checker.cancel(true);
        }
        batchObserver.onCancel();
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
        observation.close();
    }

    @Override
    public boolean isClosed() {
        return observation.isClosed();
    }

}
