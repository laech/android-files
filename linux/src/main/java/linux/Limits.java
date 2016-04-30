package linux;

public final class Limits extends Native {

    public static final int NAME_MAX = placeholder();

    static int placeholder() {
        return -1;
    }

    private Limits() {
    }

    static {
        init();
    }

    private static native void init();
}
