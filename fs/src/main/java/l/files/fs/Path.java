package l.files.fs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Arrays;

import javax.annotation.Nullable;

import static l.files.fs.Files.UTF_8;

public final class Path {

    private static final byte PATH_SEPARATOR = '/';

    /*
     * Binary representation of this path, normally it's whatever
     * returned from the native calls, unless when it's created
     * from java.lang.String.
     *
     * Using the original bytes from the OS means the path is
     * independent of charset encodings, avoiding byte loss when
     * converting from/to string, resulting certain files inaccessible.
     *
     * Currently, java.io.File suffers from the above issue, because
     * it stores the path as string internally. So it will fail to
     * handle certain files. For example, if a file whose binary file
     * name is [-19, -96, -67, -19, -80, -117], java.io.File.list on
     * the parent will return it, but any operation on that file will
     * fail.
     *
     * This class is free of the above issue.
     */
    private final byte[] path;

    private Path(byte[] path) {
        this.path = normalize(path.clone()); // Too much work here
    }

    private static byte[] normalize(byte[] path) {
        int length = lengthBySkippingEndPathSeparators(path, path.length);
        if (length == 0) {
            length = path.length;
        }
        return removeDuplicatePathSeparators(path, length);
    }

    private static byte[] removeDuplicatePathSeparators(byte[] path, int length) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(length);
        boolean skipNextPathSeparator = false;
        for (int i = 0; i < length; i++) {
            byte b = path[i];
            if (b == PATH_SEPARATOR && skipNextPathSeparator) {
                continue;
            }
            out.write(b);
            skipNextPathSeparator = (b == PATH_SEPARATOR);
        }
        return out.toByteArray();
    }

    public static Path fromFile(File file) {
        return fromString(file.getPath());
    }

    public static Path fromString(String path) {
        return fromByteArray(path.getBytes(UTF_8));
    }

    public static Path fromByteArray(byte[] path) {
        return new Path(path);
    }

    public byte[] toByteArray() {
        return path.clone();
    }

    public FileSystem fileSystem() {
        return Paths.Holder.FILE_SYSTEM;
    }

    /**
     * Returns a string representation of this path.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    @Override
    public String toString() {
        return new String(path, UTF_8);
    }

    /**
     * Converts this path to a {@link java.io.File},
     * this method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    public File toFile() {
        return new File(toString());
    }

    public Path toAbsolutePath() {
        if (isAbsolutePath()) {
            return this;
        }
        return fromString(new File("").getAbsolutePath()).resolve(this);
    }

    public boolean isAbsolutePath() {
        return path.length > 0 && path[0] == PATH_SEPARATOR;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(path);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Path &&
                Arrays.equals(path, ((Path) o).path);
    }

    // TODO call concat instead of resolve
    /**
     * Resolves the given path relative to this file.
     */
    public Path resolve(Path path) {
        return resolve(path.toByteArray());
    }

    public Path resolve(String path) {
        return resolve(path.getBytes(UTF_8));
    }

    public Path resolve(byte[] path) {
        int len = lengthBySkippingEndPathSeparators(path, path.length);
        if (len <= 0) {
            return this;
        }
        return fromByteArray(concatPaths(this.path, path, 0, len));
    }

    private static byte[] concatPaths(byte[] base, byte[] add, int addPos, int addLen) {
        if (addLen == 0) {
            return base;
        }

        byte[] joinedPath;
        if (base.length == 0 || (base[base.length - 1] != PATH_SEPARATOR && add[addPos] != PATH_SEPARATOR)) {
            joinedPath = Arrays.copyOf(base, base.length + 1 + addLen);
            joinedPath[base.length] = PATH_SEPARATOR;
            System.arraycopy(add, addPos, joinedPath, base.length + 1, addLen);

        } else {
            joinedPath = Arrays.copyOf(base, base.length + addLen);
            System.arraycopy(add, addPos, joinedPath, base.length, addLen);

        }

        return joinedPath;
    }

    @Nullable
    public Path parent() {

        int parentPathLength = path.length;
        parentPathLength = lengthBySkippingEndPathSeparators(path, parentPathLength);
        parentPathLength = lengthBySkippingNonPathSeparators(path, parentPathLength);

        int parentPathNoEndSeparatorLength = lengthBySkippingEndPathSeparators(path, parentPathLength);
        if (parentPathNoEndSeparatorLength > 0) {
            parentPathLength = parentPathNoEndSeparatorLength;
        }

        if (parentPathLength > 0) {
            return fromByteArray(Arrays.copyOfRange(path, 0, parentPathLength));
        }

        return null;
    }

    private static int lengthBySkippingEndPathSeparators(byte[] path, int fromLength) {
        while (fromLength > 0 && path[fromLength - 1] == PATH_SEPARATOR) {
            fromLength--;
        }
        return fromLength;
    }

    private static int lengthBySkippingNonPathSeparators(byte[] path, int fromLength) {
        while (fromLength > 0 && path[fromLength - 1] != PATH_SEPARATOR) {
            fromLength--;
        }
        return fromLength;
    }

    /**
     * Gets the name of this file, or empty if this is the root file.
     */
    public Path name() {
        int nameEnd = lengthBySkippingEndPathSeparators(path, path.length);
        int nameStartPos = lengthBySkippingNonPathSeparators(path, nameEnd);
        if (nameStartPos < 0) {
            nameStartPos = 0;
        }
        return fromByteArray(Arrays.copyOfRange(path, nameStartPos, nameEnd));
    }

    public boolean isHidden() {
        int nameStartPos = path.length;
        nameStartPos = lengthBySkippingEndPathSeparators(path, nameStartPos);
        nameStartPos = lengthBySkippingNonPathSeparators(path, nameStartPos);
        return nameStartPos >= 0 &&
                nameStartPos < path.length &&
                path[nameStartPos] == '.';
    }

    /**
     * Returns true if the given path is an ancestor of this path,
     * or equal to this path.
     */
    public boolean startsWith(Path p) {

        byte[] thisPath = path;
        byte[] thatPath = p.path;

        if (thatPath.length == 0 ||
                thisPath.length < thatPath.length) {
            return false;

        } else if (thisPath.length > thatPath.length) {
            if (thisPath[thatPath.length] != PATH_SEPARATOR &&
                    thatPath[thatPath.length - 1] != PATH_SEPARATOR) {
                return false;
            }
        }

        for (int i = 0; i < thatPath.length; i++) {
            if (thisPath[i] != thatPath[i]) {
                return false;
            }
        }

        return true;
    }

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
    public Path rebase(Path src, Path dst) {
        if (!startsWith(src)) {
            throw new IllegalArgumentException();
        }
        byte[] srcBytes = src.toByteArray();
        return fromByteArray(concatPaths(
                dst.toByteArray(),
                path,
                srcBytes.length,
                path.length - srcBytes.length));
    }
}
