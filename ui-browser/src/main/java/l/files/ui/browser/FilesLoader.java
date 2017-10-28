package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import javax.annotation.Nullable;

import l.files.fs.Name;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.fs.event.BatchObserver;
import l.files.fs.event.Event;
import l.files.fs.event.Observation;
import l.files.ui.base.fs.FileInfo;

import static android.os.Looper.getMainLooper;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.event.Event.DELETE;
import static l.files.ui.base.content.Contexts.isDebugBuild;

final class FilesLoader extends AsyncTaskLoader<FilesLoader.Result> {

    static final int BATCH_UPDATE_MILLIS = 1000;

    private static final Handler handler = new Handler(getMainLooper());

    private final ConcurrentMap<Name, FileInfo> data;
    private final Path root;
    private final int watchLimit;

    private final Collator collator;

    private volatile FileSort sort;
    private volatile boolean showHidden;

    private volatile boolean observing;

    @Nullable
    private volatile Observation observation;

    @Nullable
    private volatile Thread loadInBackgroundThread;

    @Nullable
    private volatile Result cachedResult;

    private final ExecutorService executor;

    private final BatchObserver listener = new BatchObserver() {

        @Override
        public void onLatestEvents(boolean selfChanged, Map<Name, Event> children) {
            if (!children.isEmpty()) {
                updateAll(children, false);
            }
        }

        @Override
        public void onIncompleteObservation(IOException cause) {
            if (isDebugBuild(getContext())) {
                Log.w("FilesLoader", "onIncompleteObservation()", cause);
            }
        }

    };

    private void updateAll(
            final Map<Name, Event> changedChildren,
            final boolean forceReload) {

        executor.execute(() -> {
            setThreadPriority(THREAD_PRIORITY_BACKGROUND);

            boolean changed = false;
            for (Entry<Name, Event> entry : changedChildren.entrySet()) {
                changed |= update(entry.getKey(), entry.getValue());
            }

            if (changed || forceReload) {
                final Result result = buildResult();
                handler.post(() -> deliverResult(result));
            }
        });
    }

    private volatile int approximateChildTotal;

    FilesLoader(
            Context context,
            Path root,
            FileSort sort,
            boolean showHidden,
            int watchLimit) {
        super(context);

        this.root = requireNonNull(root, "root");
        this.sort = requireNonNull(sort, "sort");
        this.showHidden = showHidden;
        this.data = new ConcurrentHashMap<>();
        this.executor = newSingleThreadExecutor();
        this.collator = Collator.getInstance();
        this.watchLimit = watchLimit;
    }

    int approximateChildTotal() {
        return approximateChildTotal;
    }

    int approximateChildLoaded() {
        return data.size();
    }

    void setSort(FileSort sort) {
        this.sort = requireNonNull(sort, "sort");
        updateAll(emptyMap(), true);
    }

    void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
        updateAll(emptyMap(), true);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (data.isEmpty()) {
            forceLoad();
        } else {
            Result result = this.cachedResult;
            if (result != null) {
                deliverResult(result);
            }
        }
    }

    @Override
    public void cancelLoadInBackground() {
        super.cancelLoadInBackground();
        Thread thread = loadInBackgroundThread;
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public Result loadInBackground() {
        loadInBackgroundThread = currentThread();

        if (isLoadInBackgroundCanceled()) {
            return null;
        }

        data.clear();

        boolean observe = false;
        synchronized (this) {
            if (!observing) {
                observing = true;
                observe = true;
            }
        }

        List<Name> children;
        try {
            if (observe) {
                children = observe();
            } else {
                children = visit();
            }
        } catch (IOException e) {
            return Result.of(e);

        } catch (InterruptedException e) {
            currentThread().interrupt();
            cancelLoad();
            throw new OperationCanceledException();
        }

        update(children);
        return buildResult();
    }

    private List<Name> observe() throws IOException, InterruptedException {
        List<Name> children = new ArrayList<>();
        observation = root.observe(
                FOLLOW,
                listener,
                collectInto(children),
                BATCH_UPDATE_MILLIS,
                MILLISECONDS,
                true,
                null,
                watchLimit);
        return children;
    }

    private List<Name> visit() throws IOException {
        final List<Name> children = new ArrayList<>();
        root.list((Path.Consumer) child -> {
            Name name = child.name();
            assert name != null;
            checkedAdd(children, name);
            return true;
        });
        return children;
    }

    private void checkedAdd(List<Name> children, Name child) {
        checkCancel();

        /*
         * Okay to do this without synchronization since the writer thread
         * changing this is always the same one, also because this is
         * just an approximation.
         */
        approximateChildTotal++;

        children.add(child);
    }

    private Path.Consumer collectInto(final List<Name> children) {
        return child -> {
            Name name = child.name();
            assert name != null;
            checkedAdd(children, name);
            return true;
        };
    }

    private void update(List<Name> children) {
        for (Name child : children) {
            checkCancel();
            update(child, null);
        }
    }

    private void checkCancel() {
        if (isLoadInBackgroundCanceled()) {
            throw new OperationCanceledException();
        }
    }

    private Result buildResult() {
        List<FileInfo> files = new ArrayList<>(data.size());
        if (showHidden) {
            files.addAll(data.values());
        } else {
            for (FileInfo item : data.values()) {
                if (!item.selfPath().isHidden()) {
                    files.add(item);
                }
            }
        }
        Resources res = getContext().getResources();
        List<Object> sorted = sort.sort(files, res);
        Result result = Result.of(unmodifiableList(sorted));
        cachedResult = result;
        return result;
    }

    @Override
    protected void onReset() {
        super.onReset();
        cancelLoad();

        Closeable closeable = null;
        synchronized (this) {
            if (observing) {
                closeable = observation;
                observation = null;
                observing = false;
            }
        }

        if (closeable != null) {
            executor.shutdownNow();
            try {
                closeable.close();
            } catch (IOException e) {
                Log.w(getClass().getSimpleName(),
                        "Failed to close on reset " + root, e);
            }
        }

        data.clear();
        cachedResult = null;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        Closeable closeable = null;
        synchronized (this) {
            if (observing) {
                closeable = observation;
            }
        }
        if (closeable != null) {
            executor.shutdownNow();
            closeable.close();
        }
    }

    /**
     * Adds the new status of the given path to the data map. Returns true if
     * the data map is changed.
     */
    private boolean update(Name child, @Nullable Event event) {
        return update(root.concat(child.toPath()), event);
    }

    private boolean update(Path path, @Nullable Event event) {

        /*
         * This if statement may seem unnecessary given the try-catch below
         * will remove the entry if FileNotFoundException occur anyway, but
         * this if statement is important for some case-insensitive file
         * systems.
         *
         * For example:
         *
         *  file "a" gets renamed to file "A"
         *
         * 2 events will be generated:
         *
         *   "a" -> DELETE
         *   "A" -> CREATE
         *
         * if this check is skipped, then:
         *
         *   stat("a") -> will succeed as path is case-insensitive
         *   stat("A") -> will succeed
         *
         * resulting both "a" and "A" be displayed.
         */
        if (DELETE.equals(event)) {
            return data.remove(path.name()) != null;
        }

        try {

            Stat stat = path.stat(NOFOLLOW);
            Stat targetStat = readTargetStatus(path, stat);
            Path target = readTarget(path, stat);
            FileInfo newStat = FileInfo.create(path, stat, target, targetStat, collator);
            FileInfo oldStat = data.put(path.name(), newStat);
            return !newStat.equals(oldStat);

        } catch (FileNotFoundException e) {
            return data.remove(path.name()) != null;

        } catch (IOException e) {
            data.put(
                    path.name(),
                    FileInfo.create(path, null, null, null, collator));
            return true;
        }
    }

    private Path readTarget(Path path, Stat stat) throws FileNotFoundException {
        if (stat.isSymbolicLink()) {
            try {
                return path.readSymbolicLink();
            } catch (FileNotFoundException e) {
                throw e;
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private Stat readTargetStatus(Path file, Stat stat) {
        if (stat.isSymbolicLink()) {
            try {
                return file.stat(FOLLOW);
            } catch (IOException ignored) {
            }
        }
        return stat;
    }

    static final class Result {

        private final List<Object> items;

        @Nullable
        private final IOException exception;

        private Result(List<Object> items, @Nullable IOException exception) {
            this.items = items;
            this.exception = exception;
        }

        List<Object> items() {
            return items;
        }

        @Nullable
        IOException exception() {
            return exception;
        }

        private static Result of(IOException exception) {
            return new Result(emptyList(), exception);
        }

        private static Result of(List<Object> result) {
            return new Result(result, null);
        }
    }

}
