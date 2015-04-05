package l.files.fs;


import android.os.Parcelable;

import java.net.URI;

import javax.annotation.Nullable;

/**
 * Path to a resource on a file system. A path and its URI are always absolute
 * and normalized, relative paths are not supported.
 */
public interface Path extends PathEntry, Parcelable {

    /**
     * The normalized/absolute URI of this path. Every resource has only one
     * path and one uri representation, regardless whether or not it exists on
     * the file system. For example, a traditional file URI for a directory may
     * or may not end with a "/" depending on its existence, this is disallowed
     * for implementations of this trait, as that's two representations for the
     * resource.
     */
    URI getUri();

    /**
     * Gets the parent path, returns null if this is the root path.
     */
    @Nullable
    Path getParent();

    /**
     * Gets the name part of this path, or empty if this is the root path
     */
    String getName();

    /**
     * True if this path is considered a hidden resource.
     */
    boolean isHidden();

    /**
     * Resolves the given path/name relative to this path.
     */
    Path resolve(String other);

    /**
     * Returns a new path with the given prefix replaced. Throws
     * IllegalArgumentException if this path does not start with the given
     * prefix.
     */
    Path replace(Path prefix, Path replacement);

    /**
     * Returns true if the this path starts with the given path, including if
     * the two paths are the same.
     */
    boolean startsWith(Path other);

}
