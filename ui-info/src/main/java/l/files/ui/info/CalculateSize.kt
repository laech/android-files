package l.files.ui.info

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.System.currentTimeMillis
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributes

internal data class Size(
  val count: Int,
  val size: Long,
  val done: Boolean
)

internal data class Target(
  val parent: Path,
  val childFileNames: Set<Path>
)

internal class CalculateSizeViewModel : ViewModel() {
  val target = MutableLiveData<Target>()
  val calculation = target.calculate()
}

internal fun LiveData<Target>.calculate(): LiveData<Size> =
  switchMap { (parent, children) ->
    liveData {
      calculate(children, parent)
    }
  }

private suspend fun LiveDataScope<Size>.calculate(
  childFileNames: Set<Path>,
  parent: Path
) = withContext(Dispatchers.IO) {

  var lastPostTime = 0L
  var currentCount = 0
  var currentSize = 0L

  for (child in childFileNames) {
    ensureActive()

    try {
      Files.walk(parent.resolve(child)).use {
        // TODO? handle error
        for (path in it) {
          ensureActive()

          currentCount++
          try {
            currentSize += Files.readAttributes(
              path,
              BasicFileAttributes::class.java,
              NOFOLLOW_LINKS
            ).size()
          } catch (e: IOException) {
            // TODO?
          }

          val now = currentTimeMillis()
          if (now - lastPostTime > 100) {
            lastPostTime = now
            emit(Size(currentCount, currentSize, false))
          }
        }
      }
    } catch (e: IOException) {
      // TODO?
    }
  }

  emit(Size(currentCount, currentSize, true))
}
