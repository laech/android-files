package l.files.fs

import android.os.Parcelable
import com.google.common.net.MediaType
import java.io.InputStream
import java.io.IOException
import java.io.OutputStream

trait Resource : PathEntry, Parcelable {

    override fun getResource() = this

    /**
     * Gets the name of this resource, or empty if this is the root
     */
    val name: String get() = getPath().name

    /**
     * Returns the watch service for the underlying file system.
     */
    val watcher: WatchService

    /**
     * Returns true if this resource exists, returns false if this resource
     * does not exist or failed to determine existence.
     */
    val exists: Boolean

    /**
     * Resolves the given name/path relative to this resource.
     */
    fun resolve(other: String): Resource

    /**
     * Traverse this subtree. Accepts an error handler, if the handler does not
     * rethrow the exception, traversal will continue.
     */
    throws(javaClass<IOException>())
    fun traverse(order: TraversalOrder, handler: (Resource, IOException) -> Unit): Sequence<Resource>

    /**
     * Opens a resource stream to iterate through the immediate children.
     */
    throws(javaClass<IOException>())
    fun openResourceStream(): ResourceStream<Resource>

    throws(javaClass<IOException>())
    fun openInputStream(): InputStream

    throws(javaClass<IOException>())
    fun openOutputStream(): OutputStream

    /**
     * Creates the underlying resource as a directory.
     */
    throws(javaClass<IOException>())
    fun createDirectory()

    /**
     * Creates the underlying resource as a file.
     */
    throws(javaClass<IOException>())
    fun createFile()

    /**
     * Creates the underlying resource as a symbolic link to point to the
     * given location.
     */
    throws(javaClass<IOException>())
    fun createSymbolicLink(target: Path)

    throws(javaClass<IOException>())
    fun createSymbolicLink(target: Resource) = createSymbolicLink(target.getPath())

    /**
     * If this is a symbolic link, returns the target file.
     */
    throws(javaClass<IOException>())
    fun readSymbolicLink(): Resource

    /**
     * Reads the status of this resource.
     */
    throws(javaClass<IOException>())
    fun readStatus(followLink: Boolean): ResourceStatus

    /**
     * Moves this resource tree to the given destination.
     */
    throws(javaClass<IOException>())
    fun move(dst: Path)

    throws(javaClass<IOException>())
    fun delete()

    throws(javaClass<IOException>())
    fun setLastModifiedTime(time: Long)

    /**
     * Detects the media type of the underlying file by reading it's content.
     */
    throws(javaClass<IOException>())
    fun detectMediaType(): MediaType

    enum class TraversalOrder {
        BREATH_FIRST
        PRE_ORDER
        POST_ORDER
    }

}
