package l.files.fs;

import com.google.common.primitives.Bytes;

import java.util.Arrays;

import static com.google.common.base.Charsets.UTF_8;

final class Name {

    private final byte[] bytes;

    /**
     * @throws IllegalArgumentException if name is empty, or contains '/', or contains '\0'
     */
    Name(byte[] bytes) {
        this.bytes = validateName(bytes.clone());
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
                            new String(name, UTF_8));
        }
    }

    private static void ensureContainsNoNullByte(byte[] name) {
        int i = Bytes.indexOf(name, (byte) '\0');
        if (i >= 0) {
            throw new IllegalArgumentException(
                    "Null character (index=" + i + ") is not allowed in file name: " +
                            new String(name, UTF_8));
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
        return new String(bytes, UTF_8);
    }

    public byte[] toByteArray() {
        return bytes.clone();
    }

    public boolean isHidden() {
        return bytes[0] == '.';
    }
}
