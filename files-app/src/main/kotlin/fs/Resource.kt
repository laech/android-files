package l.files.fs

import android.os.Parcelable
import com.google.common.net.MediaType
import java.io.InputStream
import java.io.IOException

trait Resource : Parcelable {

    val path: Path

    /**
     * Gets the name of this resource, or empty if this is the root
     */
    val name: String get() = path.name

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
     * Reads the status of this resource.
     */
    throws(javaClass<IOException>())
    fun stat(): FileStatus

    /**
     * Opens a new directory stream to iterate through the children of
     * this directory.
     */
    throws(javaClass<IOException>())
    fun newDirectoryStream(): DirectoryStream

    /**
     * Opens a new input stream to write to the underlying file.
     */
    throws(javaClass<IOException>())
    fun newInputStream(): InputStream

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

    /**
     * Moves this resource tree to the given destination.
     */
    throws(javaClass<IOException>())
    fun move(dst: Path)

    /**
     * Detects the media type of the underlying file by reading it's content.
     */
    throws(javaClass<IOException>())
    fun detectMediaType(): MediaType

}
