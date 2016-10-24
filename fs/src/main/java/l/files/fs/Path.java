package l.files.fs;

import com.google.common.collect.ImmutableList;

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
        ImmutableList.Builder<Name> names = ImmutableList.builder();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte b : path) {
            if (b != '/') {
                out.write(b);
            } else if (out.size() > 0) {
                names.add(new Name(out.toByteArray()));
                out.reset();
            }
        }
        if (out.size() > 0) {
            names.add(new Name(out.toByteArray()));
        }
        boolean absolute = path.length > 0 && path[0] == '/';
        RelativePath result = new RelativePath(names.build());
        if (absolute) {
            return new AbsolutePath(result);
        }
        return result;
    }

    public FileSystem fileSystem() {
        return Paths.Holder.FILE_SYSTEM;
    }

    public byte[] toByteArray() {
        return toByteArray(new ByteArrayOutputStream()).toByteArray();
    }

    abstract ByteArrayOutputStream toByteArray(ByteArrayOutputStream out);

    /**
     * Returns a string representation of this path.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    @Override
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    abstract StringBuilder toString(StringBuilder builder);

    /**
     * Converts this path to a {@link java.io.File},
     * this method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    public File toFile() {
        return new File(toString());
    }

    /**
     * If this is a relative path, converts it to an absolute path by
     * concatenating the current working directory with this path.
     * If this path is already an absolute path returns this.
     */
    abstract AbsolutePath toAbsolutePath();

    /**
     * If this is an absolute path, converts it to a relative path by
     * simply dropping the leading path separator. If this path is
     * already a relative path, returns this.
     */
    abstract RelativePath toRelativePath();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);

    /**
     * Concatenates {@code path} onto the end of this path.
     */
    public abstract Path concat(Path path);

    public Path concat(String path) {
        return concat(fromString(path));
    }

    public Path concat(byte[] path) {
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
