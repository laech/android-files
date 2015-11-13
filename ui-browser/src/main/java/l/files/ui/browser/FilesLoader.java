package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

import com.google.auto.value.AutoValue;
import com.ibm.icu.text.Collator;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.base.io.Closer;
import l.files.fs.BatchObserver;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.Name;
import l.files.fs.Observation;
import l.files.fs.Stat;
import l.files.fs.Stream;
import l.files.ui.browser.BrowserItem.FileItem;

import static android.os.Looper.getMainLooper;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.base.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

final class FilesLoader extends AsyncTaskLoader<FilesLoader.Result> {

    private static final Handler handler = new Handler(getMainLooper());

    private final ConcurrentMap<Name, FileItem> data;
    private final File root;
    private final Collator collator;

    private volatile FileSort sort;
    private volatile boolean showHidden;

    private volatile boolean observing;
    private volatile boolean autoRefreshDisabled;
    private volatile Observation observation;
    private volatile Thread loadInBackgroundThread;

    private final ExecutorService executor;

    private final BatchObserver listener = new BatchObserver() {

        @Override
        public void onBatchEvent(boolean selfChanged, Set<Name> children) {
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
            final Set<Name> changedChildren,
            final boolean forceReload) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                setThreadPriority(THREAD_PRIORITY_BACKGROUND);

                boolean changed = false;
                for (Name child : changedChildren) {
                    changed |= update(child);
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

    private final AtomicInteger approximateChildTotal = new AtomicInteger(0);

    FilesLoader(
            Context context,
            File root,
            FileSort sort,
            Collator collator,
            boolean showHidden) {
        super(context);

        this.root = requireNonNull(root, "root");
        this.sort = requireNonNull(sort, "sort");
        this.collator = requireNonNull(collator, "collator");
        this.showHidden = showHidden;
        this.data = new ConcurrentHashMap<>();
        this.executor = newSingleThreadExecutor();
    }

    boolean autoRefreshDisabled() {
        return autoRefreshDisabled;
    }

    int approximateChildTotal() {
        return approximateChildTotal.get();
    }

    int approximateChildLoaded() {
        return data.size();
    }

    void setSort(FileSort sort) {
        this.sort = requireNonNull(sort, "sort");
        updateAll(Collections.<Name>emptySet(), true);
    }

    void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
        updateAll(Collections.<Name>emptySet(), true);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (data.isEmpty()) {
            forceLoad();
        } else {
            updateAll(Collections.<Name>emptySet(), true);
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

        List<File> children;
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

    private List<File> observe() throws IOException, InterruptedException {
        List<File> children = new ArrayList<>();
        observation = root.observe(FOLLOW, listener, collectInto(children), 1, SECONDS);
        return children;
    }

    private List<File> visit() throws IOException {
        List<File> children = new ArrayList<>();
        Closer closer = Closer.create();
        try {

            Stream<File> stream = closer.register(root.list(FOLLOW));
            for (File child : stream) {
                checkedAdd(children, child);
            }

        } catch (Throwable e) {
            throw closer.rethrow(e);
        } finally {
            closer.close();
        }
        return children;
    }

    private void checkedAdd(List<File> children, File child) {
        checkCancel();
        approximateChildTotal.incrementAndGet();
        children.add(child);
    }

    private FileConsumer collectInto(final List<File> children) {
        return new FileConsumer() {
            @Override
            public void accept(File child) {
                checkedAdd(children, child);
            }
        };
    }

    private void update(List<File> children) {
        for (File child : children) {
            checkCancel();
            update(child);
        }
    }

    private void checkCancel() {
        if (isLoadInBackgroundCanceled()) {
            throw new OperationCanceledException();
        }
    }

    private Result buildResult() {
        List<FileItem> files = new ArrayList<>(data.size());
        if (showHidden) {
            files.addAll(data.values());
        } else {
            for (FileItem item : data.values()) {
                if (!item.selfFile().isHidden()) {
                    files.add(item);
                }
            }
        }
        Resources res = getContext().getResources();
        List<BrowserItem> result = sort.sort(files, res);
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
    private boolean update(Name child) {
        return update(root.resolve(child));
    }

    private boolean update(File file) {
        try {

            Stat stat = file.stat(NOFOLLOW);
            Stat targetStat = readTargetStatus(file, stat);
            File target = readTarget(file, stat);
            FileItem newStat = FileItem.create(file, stat, target, targetStat, collator);
            FileItem oldStat = data.put(file.name(), newStat);
            return !newStat.equals(oldStat);

        } catch (FileNotFoundException e) {
            return data.remove(file.name()) != null;

        } catch (IOException e) {
            data.put(
                    file.name(),
                    FileItem.create(file, null, null, null, collator));
            return true;
        }
    }

    private File readTarget(File file, Stat stat) throws FileNotFoundException {
        if (stat.isSymbolicLink()) {
            try {
                return file.readLink();
            } catch (FileNotFoundException e) {
                throw e;
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private Stat readTargetStatus(File file, Stat stat) {
        if (stat.isSymbolicLink()) {
            try {
                return file.stat(FOLLOW);
            } catch (IOException ignored) {
            }
        }
        return stat;
    }

    @AutoValue
    static abstract class Result {
        Result() {
        }

        abstract List<BrowserItem> items();

        @Nullable
        abstract IOException exception();

        private static Result of(IOException exception) {
            return new AutoValue_FilesLoader_Result(
                    Collections.<BrowserItem>emptyList(), exception);
        }

        private static Result of(List<BrowserItem> result) {
            return new AutoValue_FilesLoader_Result(result, null);
        }
    }

}
