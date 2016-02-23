package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.ibm.icu.text.Collator;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

import l.files.base.Provider;
import l.files.fs.BatchObserver;
import l.files.fs.Event;
import l.files.fs.FileConsumer;
import l.files.fs.Files;
import l.files.fs.Name;
import l.files.fs.Observation;
import l.files.fs.Path;
import l.files.fs.Stat;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.base.text.Collators;

import static android.os.Looper.getMainLooper;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Thread.currentThread;
import static java.util.Collections.emptyList;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.Event.DELETE;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

final class FilesLoader extends AsyncTaskLoader<FilesLoader.Result> {

    private static final Handler handler = new Handler(getMainLooper());

    private final ConcurrentMap<Name, FileInfo> data;
    private final Path root;

    private final Provider<Collator> collator = new Provider<Collator>() {

        // Delay initialization Collator classes until they are needed
        // the static initialization of the collator classes for the first
        // time is expensive, so do it in background thread
        private Collator instance;

        @Override
        public Collator get() {
            Collator collator = instance;
            if (collator == null) {
                collator = instance = Collators.of(Locale.getDefault());
            }
            return collator;
        }

    };


    private volatile FileSort sort;
    private volatile boolean showHidden;

    private volatile boolean observing;
    private volatile boolean autoRefreshDisabled;
    private volatile Observation observation;
    private volatile Thread loadInBackgroundThread;

    private final ExecutorService executor;

    private final BatchObserver listener = new BatchObserver() {

        @Override
        public void onLatestEvents(boolean selfChanged, Map<Name, Event> children) {
            if (!children.isEmpty()) {
                updateAll(children, false);
            }
        }

        @Override
        public void onIncompleteObservation() {
            autoRefreshDisabled = true;
        }

    };

    void updateAll(
            final Map<Name, Event> changedChildren,
            final boolean forceReload) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                setThreadPriority(THREAD_PRIORITY_BACKGROUND);

                boolean changed = false;
                for (Entry<Name, Event> entry : changedChildren.entrySet()) {
                    changed |= update(entry.getKey(), entry.getValue());
                }

                if (changed || forceReload) {
                    final Result result = buildResult();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            deliverResult(result);
                        }
                    });
                }
            }
        });
    }

    private volatile int approximateChildTotal;

    FilesLoader(
            Context context,
            Path root,
            FileSort sort,
            boolean showHidden) {
        super(context);

        this.root = requireNonNull(root, "root");
        this.sort = requireNonNull(sort, "sort");
        this.showHidden = showHidden;
        this.data = new ConcurrentHashMap<>();
        this.executor = newSingleThreadExecutor();
    }

    boolean autoRefreshDisabled() {
        return autoRefreshDisabled;
    }

    int approximateChildTotal() {
        return approximateChildTotal;
    }

    int approximateChildLoaded() {
        return data.size();
    }

    void setSort(FileSort sort) {
        this.sort = requireNonNull(sort, "sort");
        updateAll(Collections.<Name, Event>emptyMap(), true);
    }

    void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
        updateAll(Collections.<Name, Event>emptyMap(), true);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (data.isEmpty()) {
            forceLoad();
        } else {
            updateAll(Collections.<Name, Event>emptyMap(), true);
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
            e.printStackTrace();
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
        observation = Files.observe(root, FOLLOW, listener, collectInto(children), 1, SECONDS);
        return children;
    }

    private List<Name> visit() throws IOException {
        final List<Name> children = new ArrayList<>();
        Files.list(root, FOLLOW, new FileConsumer() {
            @Override
            public boolean accept(Path parent, Name child) throws IOException {
                checkedAdd(children, child);
                return true;
            }
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

    private FileConsumer collectInto(final List<Name> children) {
        return new FileConsumer() {
            @Override
            public boolean accept(Path parent, Name child) throws IOException {
                checkedAdd(children, child);
                return true;
            }
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
                if (!item.isHidden()) {
                    files.add(item);
                }
            }
        }
        Resources res = getContext().getResources();
        List<Object> result = sort.sort(files, res);
        return Result.of(result);
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
                e.printStackTrace();
            }
        }

        data.clear();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

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
            closeable.close();
        }
    }

    /**
     * Adds the new status of the given path to the data map. Returns true if
     * the data map is changed.
     */
    private boolean update(Name child, Event event) {

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
            return data.remove(child) != null;
        }

        try {

            Path path = root.resolve(child);
            Stat stat = Files.stat(path, NOFOLLOW);
            Stat targetStat = readTargetStatus(path, stat);
            Path target = readTarget(path, stat);
            FileInfo newStat = FileInfo.create(root, child, stat, target, targetStat, collator);
            FileInfo oldStat = data.put(path.name(), newStat);
            return !newStat.equals(oldStat);

        } catch (FileNotFoundException e) {
            return data.remove(child) != null;

        } catch (IOException e) {
            data.put(
                    child,
                    FileInfo.create(root, child, null, null, null, collator));
            return true;
        }
    }

    private Path readTarget(Path path, Stat stat) throws FileNotFoundException {
        if (stat.isSymbolicLink()) {
            try {
                return Files.readSymbolicLink(path);
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
                return Files.stat(file, FOLLOW);
            } catch (IOException ignored) {
            }
        }
        return stat;
    }

    static final class Result {

        private final List<Object> items;
        private final IOException exception;

        private Result(List<Object> items, IOException exception) {
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
