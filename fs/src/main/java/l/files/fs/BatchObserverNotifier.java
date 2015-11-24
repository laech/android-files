package l.files.fs;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static l.files.base.Throwables.addSuppressed;

final class BatchObserverNotifier implements Observer, Observation, Runnable {

    private static final ScheduledExecutorService service =
            newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "BatchObserverNotifier");
                }
            });

    private boolean selfChanged;
    private final Map<Name, Event> childrenChanged;
    private final BatchObserver batchObserver;

    private Observation observation;
    private ScheduledFuture<?> checker;

    BatchObserverNotifier(BatchObserver batchObserver) {
        this.batchObserver = batchObserver;
        this.childrenChanged = new HashMap<>();
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
                addSuppressed(e, sup);
            }
            throw e;

        }

        return this;
    }

    @Override
    public void onEvent(Event event, Name child) {
        synchronized (this) {

            if (child == null) {
                selfChanged = true;
            } else {
                childrenChanged.put(child, event);
            }

        }
    }

    @Override
    public void onIncompleteObservation() {
        batchObserver.onIncompleteObservation();
    }

    @Override
    public void run() {
        setThreadPriority(THREAD_PRIORITY_BACKGROUND);

        boolean snapshotSelfChanged;
        Map<Name, Event> snapshotChildrenChanged;

        synchronized (this) {

            snapshotSelfChanged = selfChanged;
            snapshotChildrenChanged = childrenChanged.isEmpty()
                    ? Collections.<Name, Event>emptyMap()
                    : Collections.unmodifiableMap(new HashMap<>(childrenChanged));

            selfChanged = false;
            childrenChanged.clear();

        }

        if (snapshotSelfChanged || !snapshotChildrenChanged.isEmpty()) {
            batchObserver.onLatestEvents(
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
