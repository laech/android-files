package l.files.fs.event;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import l.files.fs.FileSystem.Consumer;
import l.files.fs.Files;
import l.files.fs.LinkOption;
import l.files.fs.Path;

import static java.lang.System.nanoTime;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static l.files.base.Throwables.addSuppressed;

public final class BatchObserverNotifier implements Observer, Observation, Runnable {

    private static final ScheduledExecutorService service =
            newSingleThreadScheduledExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    return new Thread(r, "BatchObserverNotifier");
                }
            });

    private boolean selfChanged;
    private final Map<Path, Event> childrenChanged;
    private final BatchObserver batchObserver;
    private final long batchInterval;
    private final long batchIntervalNanos;
    private final TimeUnit batchInternalUnit;

    @Nullable
    private Observation observation;

    @Nullable
    private ScheduledFuture<?> checker;

    /**
     * If true, will deliver the new event immediate instead of waiting for next
     * schedule check to run, if the previous event was a while ago or there was
     * no previous event.
     */
    private final boolean quickNotifyFirstEvent;
    private volatile long quickNotifyLastRunNanos;

    private final String tag;
    private final int watchLimit;

    public BatchObserverNotifier(
            BatchObserver batchObserver,
            long batchInterval,
            TimeUnit batchInternalUnit,
            boolean quickNotifyFirstEvent,
            String tag,
            int watchLimit) {
        this.batchObserver = batchObserver;
        this.batchInterval = batchInterval;
        this.batchInternalUnit = batchInternalUnit;
        this.batchIntervalNanos = batchInternalUnit.toNanos(batchInterval);
        this.quickNotifyFirstEvent = quickNotifyFirstEvent;
        this.childrenChanged = new HashMap<>();
        this.tag = tag;
        this.watchLimit = watchLimit;
    }

    public Observation start(
            Path path,
            LinkOption option,
            Consumer<? super Path> childrenConsumer) throws IOException, InterruptedException {

        if (observation != null) {
            throw new IllegalStateException();
        }

        try {

            observation = Files.observe(path, option, this, childrenConsumer, tag, watchLimit);
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
    public void onEvent(Event event, @Nullable Path child) {
        synchronized (this) {

            if (child == null) {
                selfChanged = true;
            } else {
                childrenChanged.put(child, event);
            }

        }

        if (quickNotifyFirstEvent) {
            long now = nanoTime();
            if (now - quickNotifyLastRunNanos > batchIntervalNanos) {
                quickNotifyLastRunNanos = now;
                service.execute(this);
            }
        }
    }

    @Override
    public void onIncompleteObservation(IOException cause) {
        batchObserver.onIncompleteObservation(cause);
    }

    @Override
    public void run() {

        if (quickNotifyFirstEvent) {
            quickNotifyLastRunNanos = nanoTime();
        }

        boolean snapshotSelfChanged;
        Map<Path, Event> snapshotChildrenChanged;

        synchronized (this) {

            snapshotSelfChanged = selfChanged;
            snapshotChildrenChanged = childrenChanged.isEmpty()
                    ? Collections.<Path, Event>emptyMap()
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
        if (observation != null) {
            observation.close();
        }
    }

    @Override
    public boolean isClosed() {
        return observation == null || observation.isClosed();
    }

}
