package l.files.ui.browser;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.OperationCanceledException;
import android.util.Log;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import l.files.fs.event.BatchObserver;
import l.files.fs.event.BatchObserverNotifier;
import l.files.ui.base.fs.FileInfo;
import l.files.ui.browser.sort.FileSort;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static android.os.Looper.getMainLooper;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Thread.currentThread;
import static java.nio.file.Files.*;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.util.Collections.*;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

final class FilesLoader extends AsyncTaskLoader<FilesLoader.Result> {

    static final int BATCH_UPDATE_MILLIS = 1000;

    private static final Handler handler = new Handler(getMainLooper());

    private final ConcurrentMap<Path, FileInfo> data;
    private final Path root;

    private final Collator collator;

    private final Supplier<FileSort> sort;
    private final BooleanSupplier showHidden;

    private volatile boolean observing;

    @Nullable
    private volatile Closeable observation;

    @Nullable
    private volatile Thread loadInBackgroundThread;

    @Nullable
    private volatile Result cachedResult;

    private final ExecutorService executor;

    private final BatchObserver listener = childFileNames -> {
        if (!childFileNames.isEmpty()) {
            updateAll(childFileNames, false);
        }
    };

    private void updateAll(
        Map<Path, ? extends WatchEvent.Kind<?>> changedChildFileNames,
        boolean forceReload
    ) {
        executor.execute(() -> {
            setThreadPriority(THREAD_PRIORITY_BACKGROUND);

            boolean changed = false;
            for (Entry<Path, ? extends WatchEvent.Kind<?>> entry :
                changedChildFileNames.entrySet()) {
                changed |= update(entry.getKey(), entry.getValue());
            }

            if (changed || forceReload) {
                Result result = buildResult();
                handler.post(() -> deliverResult(result));
            }
        });
    }

    private volatile int approximateChildTotal;

    FilesLoader(
        Context context,
        Path root,
        Supplier<FileSort> sort,
        BooleanSupplier showHidden
    ) {
        super(context);

        this.root = requireNonNull(root, "root");
        this.sort = requireNonNull(sort, "sort");
        this.showHidden = showHidden;
        this.data = new ConcurrentHashMap<>();
        this.executor = newSingleThreadExecutor();
        this.collator = Collator.getInstance();
    }

    int approximateChildTotal() {
        return approximateChildTotal;
    }

    int approximateChildLoaded() {
        return data.size();
    }

    void updateAll() {
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

        List<Path> childFileNames;
        try {
            if (observe) {
                childFileNames = observe();
            } else {
                childFileNames = visit();
            }
        } catch (IOException e) {
            // TODO fail to observe just visit
            Log.d(getClass().getSimpleName(), "", e);
            return Result.of(e);
        }

        update(childFileNames);
        return buildResult();
    }

    private List<Path> observe() throws IOException {
        List<Path> childFileNames = new ArrayList<>();
        observation = new BatchObserverNotifier(
            listener,
            BATCH_UPDATE_MILLIS,
            MILLISECONDS,
            true
        ).start(root, collectInto(childFileNames));
        return childFileNames;
    }

    private List<Path> visit() throws IOException {
        List<Path> children = new ArrayList<>();
        try (Stream<Path> stream = Files.list(root)) {
            stream.forEach(child -> checkedAdd(children, child.getFileName()));
        }
        return children;
    }

    private void checkedAdd(List<Path> childFileNames, Path childFileName) {
        checkCancel();

        /*
         * Okay to do this without synchronization since the writer thread
         * changing this is always the same one, also because this is
         * just an approximation.
         */
        approximateChildTotal++;

        childFileNames.add(childFileName);
    }

    private Consumer<Path> collectInto(List<Path> childFileName) {
        return child -> {
            Path name = child.getFileName();
            assert name != null;
            checkedAdd(childFileName, name);
        };
    }

    private void update(List<Path> childFileNames) {
        for (Path child : childFileNames) {
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
        if (showHidden.getAsBoolean()) {
            files.addAll(data.values());
        } else {
            for (FileInfo item : data.values()) {
                try {
                    if (!isHidden(item.selfPath())) {
                        files.add(item);
                    }
                } catch (IOException e) {
                    files.add(item);
                }
            }
        }
        Resources res = getContext().getResources();
        List<Object> sorted = sort.get().sort(files, res);
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
                    "Failed to close on reset " + root, e
                );
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

    private boolean update(
        Path childFileName,
        @Nullable WatchEvent.Kind<?> kind
    ) {
        Path path = root.resolve(childFileName);

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
        if (ENTRY_DELETE.equals(kind)) {
            return data.remove(path.getFileName()) != null;
        }

        try {

            BasicFileAttributes attrs =
                readAttributes(path, BasicFileAttributes.class, NOFOLLOW_LINKS);
            BasicFileAttributes targetAttrs = readTargetStatus(path, attrs);
            Path target = readTarget(path, attrs);
            FileInfo newStat =
                FileInfo.create(path, attrs, target, targetAttrs, collator);
            FileInfo oldStat = data.put(path.getFileName(), newStat);
            return !newStat.equals(oldStat);

        } catch (FileNotFoundException | NoSuchFileException e) {
            return data.remove(path.getFileName()) != null;

        } catch (IOException e) {
            data.put(
                path.getFileName(),
                FileInfo.create(path, null, null, null, collator)
            );
            return true;
        }
    }

    private Path readTarget(Path path, BasicFileAttributes attrs)
        throws IOException {
        if (attrs.isSymbolicLink()) {
            try {
                return readSymbolicLink(path);
            } catch (FileNotFoundException | NoSuchFileException e) {
                throw e;
            } catch (IOException ignored) {
            }
        }
        return null;
    }

    private BasicFileAttributes readTargetStatus(
        Path file,
        BasicFileAttributes attrs
    ) {
        if (attrs.isSymbolicLink()) {
            try {
                return readAttributes(file, BasicFileAttributes.class);
            } catch (IOException ignored) {
            }
        }
        return attrs;
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
