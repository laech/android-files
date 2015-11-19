package l.files.ui.preview;

import static java.lang.System.arraycopy;

final class CharBuffer {

    private char[] buffer;
    private int length;

    CharBuffer() {
        buffer = new char[255];
    }

    CharBuffer(int capacity) {
        buffer = new char[capacity];
    }

    CharBuffer(String str) {
        buffer = str.toCharArray();
        length = str.length();
    }

    int length() {
        return length;
    }

    CharBuffer clear() {
        length = 0;
        return this;
    }

    CharBuffer append(Object obj) {
        String str = obj.toString();
        int newLength = str.length() + length;
        if (newLength > buffer.length) {
            reallocate(newLength);
        }
        str.getChars(0, str.length(), buffer, length);
        length = newLength;
        return this;
    }

    private void reallocate(int newLength) {
        char[] newBuffer = new char[newLength];
        arraycopy(buffer, 0, newBuffer, 0, length);
        buffer = newBuffer;
    }

    CharBuffer copy() {
        CharBuffer copy = new CharBuffer(length);
        arraycopy(buffer, 0, copy.buffer, 0, length);
        copy.length = length;
        return copy;
    }

    @Override
    public int hashCode() {
        int h = 1;
        for (int i = 0; i < length; i++) {
            h *= 1000003;
            h ^= buffer[i];
        }
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CharBuffer)) {
            return false;
        }

        CharBuffer that = (CharBuffer) o;
        if (length != that.length) {
            return false;
        }

        for (int i = 0; i < length; i++) {
            if (buffer[i] != that.buffer[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return String.valueOf(buffer, 0, length);
    }

}
