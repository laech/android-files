package l.files.fs.event;

import androidx.annotation.Nullable;
import l.files.fs.LinkOption;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.lang.System.nanoTime;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static l.files.base.Throwables.addSuppressed;

public final class BatchObserverNotifier
    implements Observer, Observation, Runnable {

    private static final ScheduledExecutorService service =
        newSingleThreadScheduledExecutor(r -> new Thread(
            r, "BatchObserverNotifier"
        ));

    private boolean selfChanged;
    private final Map<Path, Event> childFileNameChanged;
    private final BatchObserver batchObserver;
    private final long batchInterval;
    private final long batchIntervalNanos;
    private final TimeUnit batchInternalUnit;

    @Nullable
    private volatile Observation observation;

    @Nullable
    private volatile ScheduledFuture<?> checker;

    private final AtomicBoolean started;

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
        int watchLimit
    ) {
        this.batchObserver = batchObserver;
        this.batchInterval = batchInterval;
        this.batchInternalUnit = batchInternalUnit;
        this.batchIntervalNanos = batchInternalUnit.toNanos(batchInterval);
        this.quickNotifyFirstEvent = quickNotifyFirstEvent;
        this.childFileNameChanged = new HashMap<>();
        this.tag = tag;
        this.watchLimit = watchLimit;
        this.started = new AtomicBoolean(false);
    }

    public Observation start(
        Path path,
        LinkOption option,
        Consumer<Path> childrenConsumer
    ) throws IOException, InterruptedException {

        if (!started.compareAndSet(false, true)) {
            throw new IllegalStateException();
        }

        try {

            Observation ob =
                l.files.fs.Path.of(path)
                    .observe(option, this, childrenConsumer, tag, watchLimit);
            observation = ob;
            if (!ob.isClosed()) {
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
    public void onEvent(Event event, @Nullable Path childFileName) {
        synchronized (this) {

            if (childFileName == null) {
                selfChanged = true;
            } else {
                childFileNameChanged.put(childFileName, event);
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
        Map<Path, Event> snapshotChildFileNameChanged;

        synchronized (this) {

            snapshotSelfChanged = selfChanged;
            snapshotChildFileNameChanged = childFileNameChanged.isEmpty()
                ? emptyMap()
                : unmodifiableMap(new HashMap<>(childFileNameChanged));

            selfChanged = false;
            childFileNameChanged.clear();

        }

        if (snapshotSelfChanged || !snapshotChildFileNameChanged.isEmpty()) {
            batchObserver.onLatestEvents(
                snapshotSelfChanged,
                snapshotChildFileNameChanged
            );
        }

    }

    @Override
    public void close() throws IOException {
        ScheduledFuture<?> ck = this.checker;
        if (ck != null) {
            ck.cancel(true);
        }
        Observation ob = this.observation;
        if (ob != null) {
            ob.close();
        }
    }

    @Override
    public boolean isClosed() {
        Observation ob = observation;
        return ob == null || ob.isClosed();
    }

    @Nullable
    @Override
    public Throwable closeReason() {
        Observation ob = this.observation;
        return ob == null ? null : ob.closeReason();
    }

}
