package l.files.app.os;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.common.os.AsyncTaskExecutor;
import l.files.event.ShowHiddenFilesSetting;
import l.files.event.SortSetting;
import l.files.sort.Sorters;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Collections.emptyList;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.os.DirObserver.DIR_CHANGED_MASK_NO_MODIFY;
import static l.files.common.io.Files.listFiles;
import static org.apache.commons.io.filefilter.FileFilterUtils.directoryFileFilter;

final class DefaultMonitor implements Monitor {

    private static final String TAG = Monitor.class.getSimpleName();

    private final Map<File, FileObserver> mObservers = newHashMap();
    private final Multimap<File, FileObserver> mChildObservers = HashMultimap.create();

    private final Map<File, Optional<List<Object>>> mContents = newHashMap();
    private final Multimap<File, Callback> mCallbacks = HashMultimap.create();

    private final Handler mHandler = new Handler();
    private final Resources mResources;
    private final AsyncTaskExecutor executor;

    private SortSetting mSort;
    private ShowHiddenFilesSetting mShowHiddenFiles;

    private DefaultMonitor(Resources res, AsyncTaskExecutor executor) {
        this.executor = checkNotNull(executor, "executor");
        mResources = checkNotNull(res, "res");
    }

    public static DefaultMonitor create(Bus bus, Resources res, AsyncTaskExecutor executor) {
        final DefaultMonitor monitor = new DefaultMonitor(res, executor);
        bus.register(monitor);
        return monitor;
    }

    @Override
    public void register(Callback callback, final File directory) {
        mCallbacks.put(directory, callback);
        if (!mObservers.containsKey(directory)) {
            mObservers.put(directory, startNewDirObserver(directory));
            refresh(directory);
            startChildObservers(directory);
        } else {
            final Optional<List<Object>> content = mContents.get(directory);
            if (content != null) {
                callback.onRefreshed(content);
            }
        }

        if (DEBUG) {
            Log.d(TAG, toString());
        }
    }

    private void startChildObservers(final File parent) {
        executor.execute(new ChildObserverTask(parent));
    }

    private FileObserver startNewDirObserver(File directory) {
        FileObserver observer = newDirObserver(directory);
        observer.startWatching();
        return observer;
    }

    private DirObserver newDirObserver(final File directory) {
        return new DirObserver(directory, mHandler, new Runnable() {
            @Override
            public void run() {
                if (DEBUG) {
                    Log.d(TAG, "Refreshing " + directory);
                }
                refresh(directory);
            }
        });
    }

    @Override
    public void unregister(Callback callback, File directory) {
        final Collection<Callback> callbacks = mCallbacks.get(directory);
        callbacks.remove(callback);
        if (callbacks.isEmpty()) {
            mContents.remove(directory);
            mObservers.remove(directory).stopWatching();
            for (FileObserver observer : mChildObservers.removeAll(directory)) {
                observer.stopWatching();
            }
        }

        if (DEBUG) {
            Log.d(TAG, toString());
        }
    }

    Collection<Callback> getCallbacks(File dir) {
        return mCallbacks.get(dir);
    }

    FileObserver getObserver(File dir) {
        return mObservers.get(dir);
    }

    Collection<FileObserver> getChildObservers(File dir) {
        return mChildObservers.get(dir);
    }

    Optional<List<Object>> getContents(File dir) {
        return mContents.get(dir);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode() + ":"
                + "\ncallbacks:" + getCallbacksDebugString()
                + "\nobservers:" + getObserversDebugString(mObservers.values())
                + "\nchildObservers:" + getObserversDebugString(mChildObservers.values());
    }

    private String getCallbacksDebugString() {
        if (mCallbacks.isEmpty()) {
            return "{}";
        }
        final StringBuilder builder = new StringBuilder();
        for (File key : mCallbacks.keySet()) {
            builder.append("\n")
                    .append(key)
                    .append(" = ")
                    .append(mCallbacks.get(key));
        }
        return builder.toString();
    }

    private String getObserversDebugString(Collection<? extends FileObserver> observers) {
        if (observers.isEmpty()) {
            return "{}";
        }
        final StringBuilder builder = new StringBuilder();
        for (FileObserver observer : observers) {
            builder.append("\n").append(observer);
        }
        return builder.toString();
    }

    @Subscribe
    public void handle(ShowHiddenFilesSetting show) {
        if (!Objects.equal(this.mShowHiddenFiles, show)) {
            this.mShowHiddenFiles = show;
            refreshAll();
        }
    }

    @Subscribe
    public void handle(SortSetting sort) {
        if (!Objects.equal(this.mSort, sort)) {
            this.mSort = sort;
            refreshAll();
        }
    }

    private void refresh(File directory) {
        if (!allSettingsAvailable()) {
            return;
        }
        executor.execute(new RefreshTask(directory, mShowHiddenFiles.value(), mSort.value()));
    }

    private void refreshAll() {
        if (!allSettingsAvailable()) {
            return;
        }
        for (File directory : mObservers.keySet()) {
            refresh(directory);
        }
    }

    private boolean allSettingsAvailable() {
        return this.mShowHiddenFiles != null && this.mSort != null;
    }

    final class RefreshTask extends AsyncTask<Void, Void, List<Object>> {
        private final File mDirectory;
        private final boolean mShowHiddenFiles;
        private final String mSort;

        RefreshTask(File dir, boolean showHiddenFiles, String sort) {
            mDirectory = dir;
            mShowHiddenFiles = showHiddenFiles;
            mSort = sort;
        }

        @Override
        protected List<Object> doInBackground(Void... params) {
            File[] files = listFiles(mDirectory, mShowHiddenFiles);
            if (files != null) {
                return Sorters.apply(mSort, mResources, files);
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Object> result) {
            super.onPostExecute(result);
            final Optional<List<Object>> content;
            if (result == null) {
                content = Optional.absent();
            } else {
                content = Optional.<List<Object>>of(ImmutableList.copyOf(result));
            }
            mContents.put(mDirectory, content);
            for (Callback callback : mCallbacks.get(mDirectory)) {
                callback.onRefreshed(content);
            }
        }
    }

    final class ChildObserverTask extends AsyncTask<Void, Void, List<FileObserver>> {
        private final File mParent;

        ChildObserverTask(File parent) {
            this.mParent = parent;
        }

        @Override
        protected List<FileObserver> doInBackground(Void... params) {
            final File[] children = mParent.listFiles((FileFilter) directoryFileFilter());
            if (children == null) {
                return emptyList();
            }
            final List<FileObserver> observers = newArrayListWithCapacity(children.length);
            final Runnable listener = newRefreshRunnable();
            for (File child : children) {
                observers.add(new DirObserver(child, mHandler, listener, DIR_CHANGED_MASK_NO_MODIFY));
            }
            return observers;
        }

        private Runnable newRefreshRunnable() {
            return new Runnable() {
                @Override
                public void run() {
                    if (DEBUG) {
                        Log.d(TAG, "Refreshing from child observer for " + mParent);
                    }
                    refresh(mParent);
                }
            };
        }

        @Override
        protected void onPostExecute(List<FileObserver> observers) {
            super.onPostExecute(observers);
            final boolean stillValid = mObservers.get(mParent) != null;
            if (stillValid) {
                mChildObservers.putAll(mParent, observers);
                for (FileObserver observer : observers) {
                    observer.startWatching();
                }
            }
            if (DEBUG) {
                Log.d(TAG, DefaultMonitor.this.toString());
            }
        }
    }
}
