package l.files.fs.event

import java.io.IOException
import java.nio.file.Path
import java.nio.file.WatchEvent

interface Observer {

  /**
   * @param childFileName if null the event is for the observed file
   * itself, if not null the event is for the child of
   * the observed file with that this name
   */
  fun onEvent(kind: WatchEvent.Kind<*>, childFileName: Path?)

  /**
   * Called when we can no longer fully observe on all files.
   * For example, internal system limit has been reached,
   * or some files are inaccessible.
   * This maybe called multiple times.
   */
  fun onIncompleteObservation(cause: IOException)
}
