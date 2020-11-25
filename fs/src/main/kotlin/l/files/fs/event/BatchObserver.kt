package l.files.fs.event

import java.nio.file.Path
import java.nio.file.WatchEvent

interface BatchObserver {

  fun onLatestEvents(childFileNames: Map<Path, WatchEvent.Kind<*>>)

}
