package l.files.fs.local;

import java.util.Arrays;

import l.files.fs.Name;

import static l.files.base.Objects.requireNonNull;
import static l.files.fs.File.UTF_8;

public final class LocalName implements Name {

    static final byte DOT = 46; // '.' in UTF-8

    private final byte[] bytes;

    LocalName(byte[] bytes) {
        this.bytes = requireNonNull(bytes);
    }

    public static LocalName of(byte[] name) {
        return new LocalName(name);
    }

    byte[] bytes() {
        return bytes;
    }

    private int indexOfExtSeparator() {
        int i = bytes.length - 1;
        while (i >= 0 && bytes[i] != DOT) {
            i--;
        }
        return (i == -1 || i == 0 || i == bytes.length - 1) ? -1 : i;
    }

    @Override
    public String base() {
        int i = indexOfExtSeparator();
        return i != -1
                ? new String(Arrays.copyOfRange(bytes, 0, i), UTF_8)
                : toString();
    }

    @Override
    public String ext() {
        int i = indexOfExtSeparator();
        if (i == -1) {
            return "";
        }
        byte[] ext = Arrays.copyOfRange(bytes, i + 1, bytes.length);
        return new String(ext, UTF_8);
    }

    @Override
    public String dotExt() {
        String ext = ext();
        return ext.isEmpty() ? ext : "." + ext;
    }

    @Override
    public boolean isEmpty() {
        return bytes.length == 0;
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
        return o instanceof LocalName &&
                Arrays.equals(bytes, ((LocalName) o).bytes);
    }

}