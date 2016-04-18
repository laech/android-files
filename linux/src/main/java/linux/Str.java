package linux;

public final class Str extends Native {

    private Str() {
    }

    public static native String strerror(int errnum);

}
