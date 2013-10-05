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
import com.google.common.collect.SetMultimap;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import l.files.common.os.AsyncTaskExecutor;
import l.files.event.ShowHiddenFilesSetting;
import l.files.event.SortSetting;
import l.files.sort.Sorters;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static l.files.BuildConfig.DEBUG;
import static l.files.common.io.Files.listFiles;

final class DefaultMonitor implements Monitor {

    private static final String TAG = Monitor.class.getSimpleName();

    private final Map<File, FileObserver> mObservers = newHashMap();
    private final Map<File, Optional<List<Object>>> mContents = newHashMap();
    private final SetMultimap<File, Callback> mCallbacks = HashMultimap.create();

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
        final Set<Callback> callbacks = mCallbacks.get(directory);
        callbacks.remove(callback);
        if (callbacks.isEmpty()) {
            mObservers.remove(directory).stopWatching();
            mContents.remove(directory);
        }

        if (DEBUG) {
            Log.d(TAG, toString());
        }
    }

    Set<Callback> getCallbacks(File dir) {
        return mCallbacks.get(dir);
    }

    FileObserver getObserver(File dir) {
        return mObservers.get(dir);
    }

    Optional<List<Object>> getContents(File dir) {
        return mContents.get(dir);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "@" + hashCode() + ":"
                + "\ncallbacks:" + getCallbacksDebugString()
                + "\nobservers" + getObserversDebugString();
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

    private String getObserversDebugString() {
        if (mObservers.isEmpty()) {
            return "{}";
        }
        final StringBuilder builder = new StringBuilder();
        for (FileObserver observer : mObservers.values()) {
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
}
