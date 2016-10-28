package l.files.fs;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.annotation.Nullable;

import static l.files.fs.Files.UTF_8;

public abstract class Path {

    public static Path fromFile(File file) {
        return fromString(file.getPath());
    }

    public static Path fromString(String path) {
        return fromByteArray(path.getBytes(UTF_8));
    }

    public static Path fromByteArray(byte[] path) {
        RelativePath result = new RelativePath(getNames(path));
        boolean absolute = path.length > 0 && path[0] == '/';
        return absolute ? new AbsolutePath(result) : result;
    }

    private static ImmutableList<Name> getNames(byte[] path) {
        ImmutableList.Builder<Name> names = ImmutableList.builder();
        for (int start = 0, end; start < path.length; start = end + 1) {
            end = ArrayUtils.indexOf(path, (byte) '/', start);
            if (end == -1) {
                end = path.length;
            }
            if (end > start) {
                names.add(new Name(path, start, end));
            }
        }
        return names.build();
    }

    FileSystem fileSystem() {
        throw new RuntimeException("TODO");
    }

    public final byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toByteArray(out);
        return out.toByteArray();
    }

    abstract void toByteArray(ByteArrayOutputStream out);

    /**
     * Returns a string representation of this path.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        toString(builder);
        return builder.toString();
    }

    abstract void toString(StringBuilder builder);

    /**
     * Converts this path to a {@link java.io.File},
     * this method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    public final File toFile() {
        return new File(toString());
    }

    /**
     * If this is a relative path, converts it to an absolute path by
     * concatenating the current working directory with this path.
     * If this path is already an absolute path returns this.
     */
    public abstract Path toAbsolutePath();

    public abstract boolean isAbsolute();

    /**
     * Gets all the file names of this path. For example:
     * <pre>
     *     "/a/b/c" -> ["a", "b", "c"]
     * </pre>
     */
    abstract ImmutableList<Name> names();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);

    /**
     * Concatenates {@code path} onto the end of this path.
     */
    public abstract Path concat(Path path);

    public final Path concat(String path) {
        return concat(fromString(path));
    }

    public final Path concat(byte[] path) {
        return concat(fromByteArray(path));
    }

    /**
     * Returns the parent file, if any. For example:
     * <pre>
     *     "/a/b" ->  "/a"
     *     "/a"   ->  "/"   (root path, absolute)
     *     "/"    ->  null
     *     "a"    ->  ""    (current working directory, relative)
     *     ""     ->  null
     * </pre>
     */
    @Nullable
    public abstract Path parent();

    /**
     * Gets the name of this file, maybe empty but never null.
     */
    public abstract Path name();

    public abstract boolean isHidden();

    /**
     * Returns true if the given path is an ancestor of this path,
     * or equal to this path.
     */
    public abstract boolean startsWith(Path that);

    /**
     * Returns a path by replace the prefix {@code src} with {@code dst}. For example
     * <pre>
     * "/a/b".rebase("/a", "/hello") -> "/hello/b"
     * </pre>
     *
     * @throws IllegalArgumentException if {@code !this.startsWith(src)}
     */
    public abstract Path rebase(Path src, Path dst);
}
