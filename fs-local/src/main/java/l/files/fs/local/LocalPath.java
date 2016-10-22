package l.files.fs.local;

import android.os.Parcel;

import java.io.File;
import java.util.Arrays;

import l.files.fs.FileName;
import l.files.fs.FileSystem;
import l.files.fs.Path;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.Files.UTF_8;

final class LocalPath implements Path {

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

    private LocalPath(byte[] path) {
        this.path = requireNonNull(path);
    }

    public static LocalPath of(File file) {
        return of(file.getPath());
    }

    public static LocalPath of(String path) {
        return of(path.getBytes(UTF_8));
    }

    public static LocalPath of(byte[] path) {
        int length = lengthBySkippingPathSeparators(path, path.length);
        if (length > 0 && length != path.length) {
            path = Arrays.copyOfRange(path, 0, length);
        }
        return new LocalPath(path);
    }

    @Override
    public byte[] toByteArray() {
        return path.clone();
    }

    @Override
    public FileSystem fileSystem() {
        return LocalFileSystem.INSTANCE;
    }

    @Override
    public String toString() {
        return new String(path, UTF_8);
    }

    @Override
    public File toFile() {
        return new File(toString());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(path);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LocalPath &&
                Arrays.equals(path, ((LocalPath) o).path);
    }

    @Override
    public LocalPath resolve(String path) {
        return resolve(path.getBytes(UTF_8));
    }

    @Override
    public LocalPath resolve(FileName name) {
        if (name.isEmpty()) {
            return this;
        }
        return resolve(name.toByteArray());
    }

    @Override
    public LocalPath resolve(byte[] path) {
        int len = lengthBySkippingPathSeparators(path, path.length);
        if (len <= 0) {
            return this;
        }
        return new LocalPath(concatPaths(this.path, path, 0, len));
    }

    private static byte[] concatPaths(byte[] base, byte[] add, int addPos, int addLen) {
        if (addLen == 0) {
            return base;
        }

        byte[] joinedPath;
        if (base.length == 0 || (base[base.length - 1] != '/' && add[addPos] != '/')) {
            joinedPath = Arrays.copyOf(base, base.length + 1 + addLen);
            joinedPath[base.length] = '/';
            System.arraycopy(add, addPos, joinedPath, base.length + 1, addLen);

        } else {
            joinedPath = Arrays.copyOf(base, base.length + addLen);
            System.arraycopy(add, addPos, joinedPath, base.length, addLen);

        }

        return joinedPath;
    }

    @Override
    public LocalPath parent() {

        int parentPathLength = path.length;
        parentPathLength = lengthBySkippingPathSeparators(path, parentPathLength);
        parentPathLength = lengthBySkippingNonPathSeparators(path, parentPathLength);

        int parentPathNoEndSeparatorLength = lengthBySkippingPathSeparators(path, parentPathLength);
        if (parentPathNoEndSeparatorLength > 0) {
            parentPathLength = parentPathNoEndSeparatorLength;
        }

        if (parentPathLength > 0) {
            return new LocalPath(Arrays.copyOfRange(path, 0, parentPathLength));
        }

        return null;
    }

    private static int lengthBySkippingPathSeparators(byte[] path, int fromLength) {
        while (fromLength > 0 && path[fromLength - 1] == '/') {
            fromLength--;
        }
        return fromLength;
    }

    private static int lengthBySkippingNonPathSeparators(byte[] path, int fromLength) {
        while (fromLength > 0 && path[fromLength - 1] != '/') {
            fromLength--;
        }
        return fromLength;
    }

    @Override
    public FileName name() {
        int nameEnd = lengthBySkippingPathSeparators(path, path.length);
        int nameStartPos = lengthBySkippingNonPathSeparators(path, nameEnd);
        if (nameStartPos < 0) {
            nameStartPos = 0;
        }
        return FileName.fromBytes(Arrays.copyOfRange(path, nameStartPos, nameEnd));
    }

    @Override
    public boolean isHidden() {
        int nameStartPos = path.length;
        nameStartPos = lengthBySkippingPathSeparators(path, nameStartPos);
        nameStartPos = lengthBySkippingNonPathSeparators(path, nameStartPos);
        return nameStartPos >= 0 &&
                nameStartPos < path.length &&
                path[nameStartPos] == '.';
    }

    @Override
    public boolean startsWith(Path p) {

        byte[] thisPath = path;
        byte[] thatPath = ((LocalPath) p).path;

        if (thatPath.length == 0 ||
                thisPath.length < thatPath.length) {
            return false;

        } else if (thisPath.length > thatPath.length) {
            if (thisPath[thatPath.length] != '/' &&
                    thatPath[thatPath.length - 1] != '/') {
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

    @Override
    public LocalPath rebase(Path src, Path dst) {
        if (!startsWith(src)) {
            throw new IllegalArgumentException();
        }
        byte[] srcBytes = src.toByteArray();
        return new LocalPath(concatPaths(
                dst.toByteArray(),
                path,
                srcBytes.length,
                path.length - srcBytes.length));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(path);
    }

    public static final Creator<LocalPath> CREATOR = new Creator<LocalPath>() {

        @Override
        public LocalPath createFromParcel(Parcel source) {
            return new LocalPath(source.createByteArray());
        }

        @Override
        public LocalPath[] newArray(int size) {
            return new LocalPath[size];
        }

    };

}
