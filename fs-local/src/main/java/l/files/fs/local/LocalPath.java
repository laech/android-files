package l.files.fs.local;

import android.os.Parcel;

import java.util.Arrays;

import l.files.fs.Path;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.File.UTF_8;
import static l.files.fs.local.LocalName.DOT;

public final class LocalPath implements Path {

    private static final byte SEPARATOR = 47; // '/' in UTF-8

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
    private final byte[] bytes;

    LocalPath(byte[] bytes) {
        this.bytes = requireNonNull(bytes);
    }

    public static LocalPath of(byte[] path) {
        return new LocalPath(path);
    }

    public byte[] bytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return new String(bytes, UTF_8);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof LocalPath &&
                Arrays.equals(bytes, ((LocalPath) o).bytes);
    }

    LocalPath resolve(byte[] path) {

        if (path.length == 0) {
            return this;
        }

        if (path[0] == SEPARATOR) {
            return of(path);
        }

        byte[] myPath = bytes;
        byte[] newPath;
        boolean hasSeparator = myPath.length > 0 &&
                myPath[myPath.length - 1] == SEPARATOR;

        if (hasSeparator) {
            newPath = Arrays.copyOf(myPath, myPath.length + path.length);
            System.arraycopy(path, 0, newPath, myPath.length, path.length);

        } else {
            newPath = Arrays.copyOf(myPath, myPath.length + path.length + 1);
            newPath[myPath.length] = SEPARATOR;
            System.arraycopy(path, 0, newPath, myPath.length + 1, path.length);
        }

        return of(newPath);
    }

    /**
     * Returns the parent path, or null.
     */
    LocalPath parent() {
        byte[] path = bytes;
        int i = path.length - 1;
        while (i >= 0 && path[i] == SEPARATOR) i--;
        while (i >= 0 && path[i] != SEPARATOR) i--;
        if (i == 0 && path[i] == SEPARATOR) {
            return of(new byte[]{SEPARATOR});
        }

        while (i >= 0 && path[i] == SEPARATOR) i--;
        return i < 0 ? null : of(Arrays.copyOfRange(path, 0, i + 1));
    }

    @Override
    public LocalName name() {
        byte[] path = bytes;

        int nameEnd = path.length;
        while (nameEnd > 0 && path[nameEnd - 1] == SEPARATOR) {
            nameEnd--;
        }

        int nameStart = nameEnd;
        while (nameStart > 0 && path[nameStart - 1] != SEPARATOR) {
            nameStart--;
        }

        if (nameStart < 0) {
            return LocalName.of(bytes);
        }

        return LocalName.of(Arrays.copyOfRange(path, nameStart, nameEnd));
    }

    boolean isHidden() {
        byte[] path = bytes;
        int i = path.length - 1;
        while (i >= 0 && path[i] == SEPARATOR) i--;
        while (i >= 0 && path[i] != SEPARATOR) i--;
        return i < 0
                ? path.length > 0 && path[0] == DOT
                : path.length - 1 > i && path[i + 1] == DOT;
    }

    @Override
    public boolean startsWith(Path p) {

        byte[] thatPath = ((LocalPath) p).bytes;
        byte[] thisPath = bytes;

        if (thatPath.length > thisPath.length) {
            return false;
        }

        if (thatPath.length == 1 &&
                thatPath[0] == SEPARATOR &&
                thisPath[0] == SEPARATOR) {
            return true;
        }

        for (int i = 0; i < thatPath.length; i++) {
            if (thisPath[i] != thatPath[i]) {
                return false;
            }
        }

        return thatPath.length == thisPath.length ||
                thisPath[thatPath.length] == SEPARATOR;
    }

    LocalPath rebase(LocalPath src, LocalPath dst) {
        if (!startsWith(src)) {
            throw new IllegalArgumentException();
        }

        int start = src.length();
        while (start < bytes.length && bytes[start] == SEPARATOR) {
            start++;
        }
        if (start >= bytes.length) {
            return dst;
        }

        byte[] retained = Arrays.copyOfRange(bytes, start, bytes.length);
        return dst.resolve(retained);
    }

    private int length() {
        return bytes.length;
    }

    @Override
    public boolean isEmpty() {
        return length() == 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(length());
        dest.writeByteArray(bytes);
    }

    public static final Creator<LocalPath> CREATOR = new Creator<LocalPath>() {

        @Override
        public LocalPath createFromParcel(Parcel source) {
            byte[] path = new byte[source.readInt()];
            source.readByteArray(path);
            return of(path);
        }

        @Override
        public LocalPath[] newArray(int size) {
            return new LocalPath[size];
        }

    };

}
