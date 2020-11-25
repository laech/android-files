package l.files.fs.event

import java.io.IOException
import java.nio.file.Path
import java.nio.file.WatchEvent

interface BatchObserver {

  fun onLatestEvents(
    selfChanged: Boolean,
    childFileNames: Map<Path, WatchEvent.Kind<*>>
  )

  fun onIncompleteObservation(cause: IOException)
}
