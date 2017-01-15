package l.files.fs.local;

import android.os.Parcel;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

import l.files.fs.Name;

final class LocalName extends Name {

    /*
     * Binary representation of file name, normally it's whatever
     * stored on the OS, unless when it's created
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
    private final byte[] bytes;

    /**
     * @throws IllegalArgumentException if name is empty, or contains '/', or contains '\0'
     */
    LocalName(byte[] bytes, int start, int end) {
        this.bytes = validateName(Arrays.copyOfRange(bytes, start, end));
    }

    /**
     * @throws IllegalArgumentException if name is empty, or contains '/', or contains '\0'
     */
    static LocalName fromByteArray(byte[] bytes) {
        return new LocalName(bytes, 0, bytes.length);
    }

    /**
     * @throws IllegalArgumentException if name is empty, or contains '/', or contains '\0'
     */
    public static LocalName fromString(String name) {
        return fromByteArray(name.getBytes(LocalPath.ENCODING));
    }

    private static byte[] validateName(byte[] bytes) {
        ensureNotEmpty(bytes);
        ensureContainsNoPathSeparator(bytes);
        ensureContainsNoNullByte(bytes);
        return bytes;
    }

    private static void ensureNotEmpty(byte[] name) {
        if (name.length == 0) {
            throw new IllegalArgumentException("Empty name.");
        }
    }

    private static void ensureContainsNoPathSeparator(byte[] name) {
        if (Bytes.indexOf(name, (byte) '/') >= 0) {
            throw new IllegalArgumentException(
                    "Path separator '/' is not allowed in file name: " +
                            new String(name, LocalPath.ENCODING));
        }
    }

    private static void ensureContainsNoNullByte(byte[] name) {
        int i = Bytes.indexOf(name, (byte) '\0');
        if (i >= 0) {
            throw new IllegalArgumentException(
                    "Null character (index=" + i + ") is not allowed in file name: " +
                            new String(name, LocalPath.ENCODING));
        }
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

    @Override
    public String toString() {
        return new String(bytes, LocalPath.ENCODING);
    }

    @Override
    public byte[] toByteArray() {
        return bytes.clone();
    }

    @Override
    public RelativePath toPath() {
        return new RelativePath(ImmutableList.of(this));
    }

    @Override
    public boolean isHidden() {
        return bytes[0] == '.';
    }

    @Override
    public void appendTo(ByteArrayOutputStream out) {
        out.write(bytes, 0, bytes.length);
    }

    private int indexOfExtensionSeparator(int defaultValue) {
        int i = Bytes.lastIndexOf(bytes, (byte) '.');
        return (i <= 0 || i == bytes.length - 1) ? defaultValue : i;
    }

    @Override
    public String base() {
        int i = indexOfExtensionSeparator(bytes.length);
        return new String(bytes, 0, i, LocalPath.ENCODING);
    }

    @Override
    public String extension() {
        int start = indexOfExtensionSeparator(bytes.length - 1) + 1;
        int count = bytes.length - start;
        return new String(bytes, start, count, LocalPath.ENCODING);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(toByteArray());
    }

    public static final Creator<LocalName> CREATOR = new Creator<LocalName>() {

        @Override
        public LocalName createFromParcel(Parcel source) {
            return fromByteArray(source.createByteArray());
        }

        @Override
        public LocalName[] newArray(int size) {
            return new LocalName[size];
        }
    };
}
