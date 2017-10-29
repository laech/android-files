package l.files

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import l.files.BuildConfig.DEBUG
import l.files.ui.preview.Preview

class FilesApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (DEBUG) {
            StrictMode.setThreadPolicy(
                    ThreadPolicy.Builder()
                            .detectAll()
                            .penaltyDialog()
                            .penaltyLog()
                            .build())

            StrictMode.setVmPolicy(
                    VmPolicy.Builder()
                            .detectActivityLeaks()
                            .detectLeakedClosableObjects()
                            .detectLeakedRegistrationObjects()
                            .detectLeakedSqlLiteObjects()
                            .penaltyLog()
                            .build())
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
            Preview.get(this).clearThumbnailCache()
        }

        if (level >= TRIM_MEMORY_MODERATE) {
            Preview.get(this).clearBlurredThumbnailCache()
        }

    }

}
