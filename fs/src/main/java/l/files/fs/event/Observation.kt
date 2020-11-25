package l.files.fs.event

import java.io.Closeable

interface Observation : Closeable {

  val isClosed: Boolean

  fun closeReason(): Throwable?

}
