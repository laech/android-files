package l.files.base;

public final class Bytes {

    private Bytes() {
    }

    public static int indexOf(byte[] array, byte b) {
        return indexOf(array, b, 0);
    }

    public static int indexOf(byte[] array, byte b, int start) {
        if (start < 0) {
            throw new IllegalArgumentException("start=" + start);
        }
        for (int i = start; i < array.length; i++) {
            if (array[i] == b) {
                return i;
            }
        }
        return -1;
    }

    public static int lastIndexOf(byte[] array, byte b) {
        for (int i = array.length - 1; i >= 0; i--) {
            if (array[i] == b) {
                return i;
            }
        }
        return -1;
    }
}
