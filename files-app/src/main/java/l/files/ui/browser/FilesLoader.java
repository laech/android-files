package l.files.ui.browser;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;

import javax.annotation.Nullable;

import auto.parcel.AutoParcel;
import l.files.fs.Event;
import l.files.fs.NotExist;
import l.files.fs.Observer;
import l.files.fs.Resource;
import l.files.fs.Stat;
import l.files.fs.Visitor;
import l.files.logging.Logger;
import l.files.ui.browser.FileListItem.File;
import l.files.ui.browser.FileListItem.Header;

import static android.os.Looper.getMainLooper;
import static java.lang.System.nanoTime;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static java.util.concurrent.Executors.newSingleThreadScheduledExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

public final class FilesLoader extends AsyncTaskLoader<FilesLoader.Result>
{
    private static final Logger logger = Logger.get(FilesLoader.class);
    private static final Handler handler = new Handler(getMainLooper());

    private final ConcurrentMap<String, File> data;

    private final Resource root;

    private volatile FileSort sort;
    private volatile boolean showHidden;

    private volatile boolean observing;
    private volatile Closeable observable;

    private final ScheduledExecutorService executor;

    private final Set<String> childrenPendingUpdates;

    private final Runnable childrenPendingUpdatesRun = new Runnable()
    {
        long lastUpdateNanoTime = nanoTime();

        @Override
        public void run()
        {
            /* Don't update if last update was less than a second ago,
             * this avoid updating too frequently due to resources in the
             * current directory being changed frequently by other processes,
             * but since user triggered operation are usually seconds apart,
             * those actions will still be updated instantly.
             */
            final long now = nanoTime();
            if (now - lastUpdateNanoTime < SECONDS.toNanos(1))
            {
                return;
            }

            final String[] children;
            synchronized (FilesLoader.this)
            {
                if (childrenPendingUpdates.isEmpty())
                {
                    return;
                }
                children = new String[childrenPendingUpdates.size()];
                childrenPendingUpdates.toArray(children);
                childrenPendingUpdates.clear();
            }

            lastUpdateNanoTime = now;

            boolean changed = false;
            for (final String child : children)
            {
                changed |= update(child);
            }

            if (changed)
            {
                final Result result = buildResult();
                handler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        deliverResult(result);
                    }
                });
            }
        }
    };


    private final Observer listener = new Observer()
    {
        @Override
        public void onEvent(final Event event, final String child)
        {
            if (child != null)
            {
                synchronized (FilesLoader.this)
                {
                    childrenPendingUpdates.add(child);
                }
            }
        }
    };

    /**
     * @param root
     *         the resource to load files from
     * @param sort
     *         the comparator for sorting results
     * @param showHidden
     *         whether to show hidden files
     */
    public FilesLoader(
            final Context context,
            final Resource root,
            final FileSort sort,
            final boolean showHidden)
    {
        super(context);

        this.root = requireNonNull(root, "root");
        this.sort = requireNonNull(sort, "sort");
        this.showHidden = showHidden;
        this.data = new ConcurrentHashMap<>();
        this.childrenPendingUpdates = new HashSet<>();
        this.executor = newSingleThreadScheduledExecutor();
    }

    public void setSort(final FileSort sort)
    {
        this.sort = requireNonNull(sort, "sort");
        startLoading();
    }

    public void setShowHidden(final boolean showHidden)
    {
        this.showHidden = showHidden;
        startLoading();
    }

    @Override
    protected void onStartLoading()
    {
        super.onStartLoading();
        if (data.isEmpty())
        {
            forceLoad();
        }
        else
        {
            deliverResult(buildResult());
        }
    }

    @Override
    public Result loadInBackground()
    {
        data.clear();

        boolean observe = false;
        synchronized (this)
        {
            if (!observing)
            {
                observing = true;
                observe = true;
            }
        }

        if (observe)
        {
            try
            {
                observable = root.observe(FOLLOW, listener);
            }
            catch (final IOException e)
            {
                logger.debug(e);
                return Result.of(e);
            }

            executor.scheduleWithFixedDelay(
                    childrenPendingUpdatesRun, 80, 80, MILLISECONDS);
        }

        try
        {
            root.list(FOLLOW, new Visitor()
            {
                @Override
                public Result accept(final Resource resource) throws IOException
                {
                    if (isLoadInBackgroundCanceled())
                    {
                        return TERMINATE;
                    }
                    update(resource);
                    return CONTINUE;
                }
            });
        }
        catch (final IOException e)
        {
            logger.debug(e);
            return Result.of(e);
        }

        return buildResult();
    }

    private Result buildResult()
    {
        final List<File> files = new ArrayList<>(data.size());
        if (showHidden)
        {
            files.addAll(data.values());
        }
        else
        {
            for (final File item : data.values())
            {
                if (!item.resource().hidden())
                {
                    files.add(item);
                }
            }
        }
        Collections.sort(files, sort.newComparator(Locale.getDefault()));

        final List<FileListItem> result = new ArrayList<>(files.size() + 6);

        final Categorizer categorizer = sort.newCategorizer();
        final Resources res = getContext().getResources();
        String preCategory = null;
        for (int i = 0; i < files.size(); i++)
        {
            final File stat = files.get(i);

            // TODO make this fast O(n) to O(logN)
            final String category = categorizer.get(res, stat);
            if (i == 0)
            {
                if (category != null)
                {
                    result.add(Header.of(category));
                }
            }
            else
            {
                if (!Objects.equals(preCategory, category))
                {
                    result.add(Header.of(category));
                }
            }
            result.add(stat);
            preCategory = category;
        }

        return Result.of(unmodifiableList(result));
    }

    @Override
    protected void onReset()
    {
        super.onReset();

        Closeable closeable = null;
        synchronized (this)
        {
            if (observing)
            {
                closeable = observable;
                observable = null;
                observing = false;
            }
        }

        if (closeable != null)
        {
            executor.shutdownNow();
            try
            {
                closeable.close();
            }
            catch (final IOException e)
            {
                logger.warn(e);
            }
        }

        data.clear();
    }

    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();

        Closeable closeable = null;
        synchronized (this)
        {
            if (observing)
            {
                closeable = observable;
                observable = null;
                observing = false;
            }
        }
        if (closeable != null)
        {
            logger.error("Has not been unregistered");
            executor.shutdownNow();
            closeable.close();
        }
    }

    /**
     * Adds the new status of the given path to the data map. Returns true if
     * the data map is changed.
     */
    private boolean update(final String child)
    {
        return update(root.resolve(child));
    }

    private boolean update(final Resource resource)
    {
        try
        {
            final Stat stat = resource.stat(NOFOLLOW);
            final Stat targetStat = readTargetStatus(resource, stat);
            final File newStat = File.create(resource, stat, targetStat);
            final File oldStat = data.put(resource.name().toString(), newStat);
            return !Objects.equals(newStat, oldStat);
        }
        catch (final NotExist e)
        {
            return data.remove(resource.name().toString()) != null;
        }
        catch (final IOException e)
        {
            data.put(
                    resource.name().toString(),
                    File.create(resource, null, null));
            return true;
        }
    }

    private Stat readTargetStatus(final Resource resource, final Stat stat)
    {
        if (stat.isSymbolicLink())
        {
            try
            {
                return resource.stat(FOLLOW);
            }
            catch (final IOException e)
            {
                logger.debug(e);
            }
        }
        return stat;
    }

    @AutoParcel
    static abstract class Result
    {
        Result()
        {
        }

        abstract List<FileListItem> items();

        @Nullable
        abstract IOException exception();

        private static Result of(final IOException exception)
        {
            return new AutoParcel_FilesLoader_Result(
                    Collections.<FileListItem>emptyList(), exception);
        }

        private static Result of(final List<FileListItem> result)
        {
            return new AutoParcel_FilesLoader_Result(result, null);
        }
    }

}
