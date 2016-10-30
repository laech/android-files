package l.files.fs;

import com.google.common.primitives.Bytes;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

final class Name {

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
    Name(byte[] bytes, int start, int end) {
        this.bytes = validateName(Arrays.copyOfRange(bytes, start, end));
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
                            new String(name, Path.stringEncoding));
        }
    }

    private static void ensureContainsNoNullByte(byte[] name) {
        int i = Bytes.indexOf(name, (byte) '\0');
        if (i >= 0) {
            throw new IllegalArgumentException(
                    "Null character (index=" + i + ") is not allowed in file name: " +
                            new String(name, Path.stringEncoding));
        }
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Name &&
                Arrays.equals(bytes, ((Name) o).bytes);
    }

    @Override
    public String toString() {
        return new String(bytes, Path.stringEncoding);
    }

    public byte[] toByteArray() {
        return bytes.clone();
    }

    public boolean isHidden() {
        return bytes[0] == '.';
    }

    public void appendTo(ByteArrayOutputStream out) {
        out.write(bytes, 0, bytes.length);
    }
}
