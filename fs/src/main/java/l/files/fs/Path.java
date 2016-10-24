package l.files.fs;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.UnmodifiableIterator;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.min;
import static l.files.fs.Files.UTF_8;

public class Path {

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
     */
    private final ImmutableList<Name> names;

    private final boolean absolute;

    Path(boolean absolute, ImmutableList<Name> names) {
        this.absolute = absolute;
        this.names = checkNotNull(names);
    }

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
        return new Path(absolute, names.build());
    }

    public byte[] toByteArray() {
        return toByteArray(new ByteArrayOutputStream()).toByteArray();
    }

    ByteArrayOutputStream toByteArray(ByteArrayOutputStream out) {
        if (absolute) {
            out.write('/');
        }
        UnmodifiableIterator<Name> iterator = names.iterator();
        while (iterator.hasNext()) {
            try {
                out.write(iterator.next().toByteArray());
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            if (iterator.hasNext()) {
                out.write('/');
            }
        }
        return out;
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
        return toString(new StringBuilder()).toString();
    }

    StringBuilder toString(StringBuilder builder) {
        if (absolute) {
            builder.append('/');
        }
        Joiner.on('/').appendTo(builder, names);
        return builder;
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
        if (absolute) {
            return this;
        }
        return fromString(new File("").getAbsolutePath()).concat(this);
    }

    @Override
    public int hashCode() {
        int result = names.hashCode();
        result = 31 * result + (absolute ? 1 : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Path &&
                absolute == ((Path) o).absolute &&
                names.equals(((Path) o).names);
    }

    /**
     * Resolves the given path relative to this file.
     */
    public Path concat(Path path) {
        return new Path(absolute, ImmutableList.<Name>builder()
                .addAll(names)
                .addAll(path.names)
                .build());
    }

    public Path concat(String path) {
        return concat(fromString(path));
    }

    public Path concat(byte[] path) {
        return concat(fromByteArray(path));
    }

    @Nullable
    public Path parent() {
        if (names.isEmpty()) {
            return null;
        }
        if (names.size() == 1 && !absolute) {
            return null;
        }
        return new Path(absolute, names.subList(0, names.size() - 1));
    }

    /**
     * Gets the name of this file, or empty if this is the root file.
     */
    public Path name() {
        return new Path(false, names.reverse().subList(0, min(1, names.size())));
    }

    public boolean isHidden() {
        return !names.isEmpty() && names.get(names.size() - 1).isHidden();
    }

    /**
     * Returns true if the given path is an ancestor of this path,
     * or equal to this path.
     */
    public boolean startsWith(Path that) {
        if (absolute != that.absolute) {
            return false;
        }
        if (that.names.size() > names.size()) {
            return false;
        }
        if (!absolute && that.names.isEmpty() != names.isEmpty()) {
            return false;
        }
        return names.subList(0, that.names.size()).equals(that.names);
    }

    /**
     * Returns a file with the given parent replaced.
     * <p/>
     * e.g.
     * <pre>
     * File("/a/b").concat(File("/a"), File("/c")) =
     * File("/c/b")
     * </pre>
     *
     * @throws IllegalArgumentException if {@code !this.startsWith(src)}
     */
    public Path rebase(Path src, Path dst) {
        if (!startsWith(src)) {
            throw new IllegalArgumentException();
        }
        return dst.concat(new Path(absolute, names.subList(src.names.size(), names.size())));
    }
}
