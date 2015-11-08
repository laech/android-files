package l.files.base;

public final class Objects {
    private Objects() {
    }

    public static <T> T requireNonNull(T o) {
        if (o == null) {
            throw new NullPointerException();
        }
        return o;
    }

    public static <T> T requireNonNull(T o, String message) {
        if (o == null) {
            throw new NullPointerException(message);
        }
        return o;
    }

    public static boolean equal(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }

}
