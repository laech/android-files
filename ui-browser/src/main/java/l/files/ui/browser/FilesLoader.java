package l.files.ui.browser;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.support.annotation.Nullable;

import com.google.auto.value.AutoValue;
import com.ibm.icu.text.Collator;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import l.files.fs.BatchObserver;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.Observation;
import l.files.fs.Stat;
import l.files.fs.Stream;

import static android.os.Looper.getMainLooper;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class FilesLoader extends AsyncTaskLoader<FilesLoader.Result> {

    private static final Handler handler = new Handler(getMainLooper());

    private final ConcurrentMap<String, FileListItem.File> data;
    private final File root;
    private final Collator collator;

    private volatile FileSort sort;
    private volatile boolean showHidden;

    private volatile boolean observing;
    private volatile boolean autoRefreshEnabled;
    private volatile Observation observation;
    private volatile Thread loadInBackgroundThread;

    private final ExecutorService executor;

    private final BatchObserver listener = new BatchObserver() {

        @Override
        public void onBatchEvent(boolean selfChanged, final Set<String> children) {
            if (!children.isEmpty()) {
                updateAll(children, false);
            }
        }

        @Override
        public void onCancel() {
            autoRefreshEnabled = false;
            updateAll(Collections.<String>emptySet(), true);
        }

    };

    void updateAll(
            final Set<String> changedChildren,
            final boolean forceReload) {

        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean changed = false;
                for (String child : changedChildren) {
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

    public FilesLoader(
            Context context,
            File root,
            FileSort sort,
            Collator collator,
            boolean showHidden,
            boolean attemptAutoRefreshEnabled) {
        super(context);

        this.root = requireNonNull(root, "root");
        this.sort = requireNonNull(sort, "sort");
        this.collator = requireNonNull(collator, "collator");
        this.showHidden = showHidden;
        this.autoRefreshEnabled = attemptAutoRefreshEnabled;
        this.data = new ConcurrentHashMap<>();
        this.executor = newSingleThreadExecutor();
    }

    public int approximateChildTotal() {
        return approximateChildTotal.get();
    }

    public int approximateChildLoaded() {
        return data.size();
    }

    public void setSort(FileSort sort) {
        this.sort = requireNonNull(sort, "sort");
        updateAll(Collections.<String>emptySet(), true);
    }

    public void setShowHidden(boolean showHidden) {
        this.showHidden = showHidden;
        updateAll(Collections.<String>emptySet(), true);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        if (data.isEmpty()) {
            forceLoad();
        } else {
            updateAll(Collections.<String>emptySet(), true);
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
            if (observe && autoRefreshEnabled) {
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
        autoRefreshEnabled = !observation.isClosed();
        return children;
    }

    private List<File> visit() throws IOException {
        List<File> children = new ArrayList<>();
        try (Stream<File> stream = root.list(FOLLOW)) {
            for (File child : stream) {
                checkedAdd(children, child);
            }
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
        List<FileListItem.File> files = new ArrayList<>(data.size());
        if (showHidden) {
            files.addAll(data.values());
        } else {
            for (FileListItem.File item : data.values()) {
                if (!item.file().isHidden()) {
                    files.add(item);
                }
            }
        }
        Resources res = getContext().getResources();
        List<FileListItem> result = sort.sort(files, res);
        return Result.of(result, autoRefreshEnabled);
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
    private boolean update(String child) {
        return update(root.resolve(child));
    }

    private boolean update(File file) {
        try {

            Stat stat = file.stat(NOFOLLOW);
            Stat targetStat = readTargetStatus(file, stat);
            File target = readTarget(file, stat);
            FileListItem.File newStat = FileListItem.File.create(file, stat, target, targetStat, collator);
            FileListItem.File oldStat = data.put(file.name().toString(), newStat);
            return !Objects.equals(newStat, oldStat);

        } catch (FileNotFoundException e) {
            return data.remove(file.name().toString()) != null;

        } catch (IOException e) {
            data.put(
                    file.name().toString(),
                    FileListItem.File.create(file, null, null, null, collator));
            return true;
        }
    }

    private File readTarget(File file, Stat stat) {
        if (stat.isSymbolicLink()) {
            try {
                return file.readLink();
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

        abstract List<FileListItem> items();

        abstract boolean autoRefreshEnabled();

        @Nullable
        abstract IOException exception();

        private static Result of(IOException exception) {
            return new AutoValue_FilesLoader_Result(
                    Collections.<FileListItem>emptyList(), false, exception);
        }

        private static Result of(
                List<FileListItem> result, boolean autoRefreshEnabled) {

            return new AutoValue_FilesLoader_Result(
                    result, autoRefreshEnabled, null);
        }
    }

}