package l.files.fs

import java.io.IOException
import java.io.Closeable

/**
 * Service designed for monitoring file changes within a directory.
 *
 * When registering on a directory, all files/directories of the given directory
 * will be monitored for changes (non-recursive), including file
 * addition/modification/deletion and anything that changes the attribute of the
 * files/directories. This is non-recursive, meaning only changes to
 * files/directories at the current depth level will be reported.
 *
 * Note that by the time a listener is notified, the target file may have
 * already be changed, therefore a robust application should have an alternative
 * way of handling instead of reply on this fully.
 */
trait WatchService : Closeable {

    /**
     * Starts monitoring on the given path, file systems event on the given
     * path will be sent to the given registered listeners. Does nothing if the
     * listener is already registered for the given path. Note that if the path
     * is a directory, attribute change events of the directory itself will also
     * be reported.
     */
    throws(javaClass<IOException>())
    fun register(path: Path, listener: WatchEvent.Listener)

    /**
     * Stops monitoring on the given path. Does nothing if the listener is not
     * registered for the given path.
     */
    fun unregister(path: Path, listener: WatchEvent.Listener)

    /**
     * Returns true if the given path is registered.
     */
    fun isRegistered(path: Path): Boolean

    /**
     * Returns true if the path can be watched, false otherwise.
     */
    fun isWatchable(path: Path): Boolean

}
