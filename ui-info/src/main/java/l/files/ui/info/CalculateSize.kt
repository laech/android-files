package l.files.ui.info

import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import l.files.fs.*
import l.files.fs.LinkOption.NOFOLLOW
import java.io.IOException
import java.lang.System.currentTimeMillis

internal data class Size(
  val count: Int,
  val size: Long,
  val done: Boolean
)

internal data class Target(
  val parent: Path,
  val children: Set<Name>
)

internal class CalculateSizeViewModel : ViewModel() {
  val target = MutableLiveData<Target>()
  val calculation = target.calculate()
}

internal fun LiveData<Target>.calculate(): LiveData<Size> =
  switchMap { (parent, children) ->
    liveData<Size> {
      calculate(children, parent)
    }
  }

private suspend fun LiveDataScope<Size>.calculate(
  children: Set<Name>,
  parent: Path
) = withContext(Dispatchers.IO) {

  var lastPostTime = 0L
  var currentCount = 0
  var currentSize = 0L

  for (child in children) {
    ensureActive()

    try {
      parent.concat(child).traverse(TraverseOrder.PRE).use {
        // TODO? handle error
        for ((path, _) in it.iterator()) {
          ensureActive()

          currentCount++
          try {
            currentSize += path.stat(NOFOLLOW).size()
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
