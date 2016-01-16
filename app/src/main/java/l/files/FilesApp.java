package l.files;

import android.app.Application;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.VmPolicy;
import android.util.Log;

import java.util.Locale;

import l.files.ui.base.text.Collators;
import l.files.ui.browser.FilesActivity;
import l.files.ui.preview.Preview;

import static l.files.BuildConfig.DEBUG;

public final class FilesApp extends Application {

    @Override
    public void onCreate() {

        startInitCollationClassesInBackground();

        super.onCreate();

        FilesActivity.DEBUG_UI = DEBUG;

        if (DEBUG) {
            StrictMode.setThreadPolicy(
                    new ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyDialog()
                            .penaltyLog()
                            .build());

            StrictMode.setVmPolicy(
                    new VmPolicy.Builder()
                            .detectActivityLeaks()
                            .detectLeakedClosableObjects()
                            .detectLeakedRegistrationObjects()
                            .detectLeakedSqlLiteObjects()
                            .penaltyLog()
                            .build());
        }
    }

    private void startInitCollationClassesInBackground() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                // Static initializations of the collator classes
                // for the first time is expensive
                Collators.of(Locale.getDefault());
            }
        });
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
            Preview.get(this).clearThumbnailCache();
            System.gc();
        }

        if (level >= TRIM_MEMORY_MODERATE) {
            Preview.get(this).clearBlurredThumbnailCache();
            System.gc();
        }

        if (DEBUG) {
            Log.d(getClass().getSimpleName(), "onTrimMemory(" + level + ")");
        }
    }

}
