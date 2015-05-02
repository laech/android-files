package l.files.ui.browser;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;

import com.google.common.collect.ImmutableList;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

import static android.os.Looper.getMainLooper;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Visitor.Result.CONTINUE;
import static l.files.fs.Visitor.Result.TERMINATE;

public final class FilesLoader extends AsyncTaskLoader<FilesLoader.Result>
{
    private static final Logger logger = Logger.get(FilesLoader.class);
    private static final Handler handler = new Handler(getMainLooper());

    private final ConcurrentMap<String, File> data;
    private final EventListener listener;
    private final Runnable deliverResult;

    private final Resource root;

    private volatile FileSort sort;
    private volatile boolean showHidden;

    private volatile boolean observing;
    private volatile Closeable observable;

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
        this.listener = new EventListener();
        this.deliverResult = new DeliverResultRunnable();
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
                    addData(resource);
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
                if (!item.getResource().hidden())
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
            final String category = categorizer.get(res, stat);
            if (i == 0)
            {
                if (category != null)
                {
                    result.add(FileListItem.Header.create(category));
                }
            }
            else
            {
                if (!Objects.equals(preCategory, category))
                {
                    result.add(FileListItem.Header.create(category));
                }
            }
            result.add(stat);
            preCategory = category;
        }

        return Result.of(result);
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
            logger.error("WatchService has not been unregistered");
            closeable.close();
        }
    }

    /**
     * Adds the new status of the given path to the data map. Returns true if
     * the data map is changed.
     */
    private boolean addData(final String child)
    {
        return addData(root.resolve(child));
    }

    private boolean addData(final Resource resource)
    {
        try
        {
            final Stat stat = resource.stat(NOFOLLOW);
            final Stat targetStat = readTargetStatus(resource, stat);
            final File newStat = File.create(resource, stat, targetStat);
            final File oldStat = data.put(resource.name(), newStat);
            return !Objects.equals(newStat, oldStat);
        }
        catch (final NotExist e)
        {
            return data.remove(resource.name()) != null;
        }
        catch (final IOException e)
        {
            data.put(resource.name(), File.create(resource, null, null));
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

    // TODO delay with batch update instead of updating for every event
    // to avoid the case of too many events coming in at the same time
    final class EventListener implements Observer
    {
        @Override
        public void onEvent(final Event event, @Nullable final String child)
        {
            final boolean isChild = child != null;
            switch (event)
            {
                case CREATE:
                case MODIFY:
                    if (isChild && addData(child))
                    {
                        redeliverResult();
                    }
                    break;
                case DELETE:
                    if (isChild && data.remove(child) != null)
                    {
                        redeliverResult();
                    }
                    break;
                default:
                    throw new AssertionError(event);
            }
        }
    }

    private void redeliverResult()
    {
        handler.removeCallbacks(deliverResult);
        handler.postDelayed(deliverResult, 100);
    }

    final class DeliverResultRunnable implements Runnable
    {
        @Override
        public void run()
        {
            deliverResult(buildResult());
        }
    }

    @AutoParcel
    static abstract class Result
    {
        Result()
        {
        }

        abstract List<FileListItem> getItems();

        @Nullable
        abstract IOException getException();

        static Result of(final IOException exception)
        {
            return new AutoParcel_FilesLoader_Result(
                    Collections.<FileListItem>emptyList(), exception);
        }

        static Result of(final List<? extends FileListItem> result)
        {
            return new AutoParcel_FilesLoader_Result(
                    ImmutableList.copyOf(result), null);
        }
    }

}
