package l.files.fs

import android.os.Parcelable

import java.net.URI

/**
 * Path to a resource on a file system. A path and its `uri` are always
 * absolute and normalized, relative paths are not supported.
 */
trait Path : PathEntry, Parcelable {

    override fun getPath() = this

    /**
     * The normalized/absolute URI of this path.
     * Every resource has only one path and one uri representation,
     * regardless whether or not it exists on the file system.
     * For example, a traditional file URI for a directory may or may not end with
     * a "/" depending on its existence, this is disallowed for implementations of
     * this trait, as that's two representations for the resource.
     */
    val uri: URI

    /**
     * Gets the parent path, returns null if this is the root path.
     */
    val parent: Path?

    /**
     * Gets the name part of this path, or empty if this is the root path
     */
    val name: String

    /**
     * True if this path is considered a hidden resource.
     */
    val isHidden: Boolean

    /**
     * Resolves the given path/name relative to this path.
     */
    fun resolve(other: String): Path

    /**
     * Returns a new path with the given prefix replaced.
     * Throws IllegalArgumentException if this path does not start with the
     * given prefix.
     */
    fun replace(prefix: Path, new: Path): Path

    /**
     * Returns true if the this path starts with the given path, including if
     * the two paths are the same.
     */
    fun startsWith(other: Path): Boolean

}
