package l.files.app;

import android.app.Application;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import com.google.common.collect.ImmutableSet;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;
import l.files.app.os.Monitor;
import l.files.app.os.Monitors;
import l.files.common.os.AsyncTaskExecutor;

import java.io.File;
import java.util.Set;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.google.common.collect.Sets.newHashSet;
import static l.files.BuildConfig.DEBUG;
import static l.files.app.UserDirs.*;
import static l.files.event.Events.*;
import static l.files.sort.Sorters.NAME;

public final class FilesApp extends Application {

    // TODO
    private static final Set<File> DEFAULT_BOOKMARKS = ImmutableSet.of(
            DIR_DCIM,
            DIR_MUSIC,
            DIR_MOVIES,
            DIR_PICTURES,
            DIR_DOWNLOADS);

    public static Bus getBus(Fragment fragment) {
        return getBus(fragment.getActivity());
    }

    public static Bus getBus(Context context) {
        return ((FilesApp) context.getApplicationContext()).mBus;
    }

    public static Monitor getMonitor(Fragment fragment) {
        return getMonitor(fragment.getActivity());
    }

    public static Monitor getMonitor(Context context) {
        return ((FilesApp) context.getApplicationContext()).mMonitor;
    }

    private Bus mBus;
    private Monitor mMonitor;

    @Override
    public void onCreate() {
        super.onCreate();

        mBus = new Bus(ThreadEnforcer.MAIN) {

            // TODO
            private final Set<Object> objects = newHashSet();

            @Override
            public void register(Object object) {
                if (objects.add(object)) super.register(object);
            }

            @Override
            public void unregister(Object object) {
                if (objects.remove(object)) super.unregister(object);
            }

        };
        mMonitor = Monitors.create(mBus, getResources(), AsyncTaskExecutor.DEFAULT);
        SharedPreferences pref = getDefaultSharedPreferences(this);
        registerSortProvider(mBus, pref, NAME);
        registerBookmarksProvider(mBus, pref, DEFAULT_BOOKMARKS);
        registerShowHiddenFilesProvider(mBus, pref, false);
        registerClipboardProvider(mBus, getClipboardManager());
        registerIoProvider(mBus, this);

        if (DEBUG) {
            StrictMode.enableDefaults();
        }
    }

    private ClipboardManager getClipboardManager() {
        return (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }
}
