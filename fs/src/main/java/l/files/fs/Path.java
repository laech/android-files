package l.files.fs;

import android.os.Parcelable;

import java.io.File;

import javax.annotation.Nullable;

public interface Path extends Parcelable {

    FileSystem fileSystem();

    /**
     * Returns a string representation of this path.
     * <p/>
     * This method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    @Override
    String toString();

    /**
     * Converts this path to a {@link java.io.File},
     * this method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    File toFile();

    byte[] toByteArray();

    /**
     * Gets the name of this file, or empty if this is the root file.
     */
    FileName name();

    @Nullable
    Path parent();

    /**
     * Resolves the given path relative to this file.
     */
    Path resolve(String path);

    Path resolve(byte[] path);

    Path resolve(FileName name);

    /**
     * Returns a file with the given parent replaced.
     * <p/>
     * e.g.
     * <pre>
     * File("/a/b").resolve(File("/a"), File("/c")) =
     * File("/c/b")
     * </pre>
     *
     * @throws IllegalArgumentException if {@code !this.startsWith(src)}
     */
    Path rebase(Path src, Path dst);

    /**
     * Returns true if the given path is an ancestor of this path,
     * or equal to this path.
     */
    boolean startsWith(Path p);

    boolean isHidden();
}
