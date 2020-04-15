package l.files

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import l.files.ui.preview.getPreview

class FilesApp : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {

      StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
          .detectAll()
          .penaltyDialog()
          .penaltyLog()
          .build()
      )

      StrictMode.setVmPolicy(
        VmPolicy.Builder()
          .detectActivityLeaks()
          .detectLeakedClosableObjects()
          .detectLeakedRegistrationObjects()
          .detectLeakedSqlLiteObjects()
          .penaltyLog()
          .build()
      )
    }
  }

  override fun onTrimMemory(level: Int) {
    super.onTrimMemory(level)
    if (level >= TRIM_MEMORY_RUNNING_CRITICAL) {
      getPreview().clearThumbnailCache()
    }
    if (level >= TRIM_MEMORY_MODERATE) {
      getPreview().clearBlurredThumbnailCache()
    }
  }
}
