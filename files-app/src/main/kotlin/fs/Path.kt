package l.files.fs

import android.os.Parcelable

import java.net.URI

import l.files.fs.Resource

/**
 * Path to a resource on a file system. A path and its `uri` are always
 * absolute and normalized, relative paths are not supported.
 */
trait Path : Parcelable {

    val resource: Resource

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
     * Resolves the given path/name relative to this path.
     */
    fun resolve(other: String): Path

    /**
     * Returns true if the this path starts with the given path, including if
     * the two paths are the same.
     */
    fun startsWith(other: Path): Boolean

}
