package l.files.fs.event

import java.nio.file.Path
import java.nio.file.WatchEvent

interface Observer {

  fun onEvent(kind: WatchEvent.Kind<*>, childFileName: Path)

}
