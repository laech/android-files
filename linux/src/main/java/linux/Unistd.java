package linux;

public final class Unistd extends Native {

    public static final byte R_OK = placeholder();
    public static final byte W_OK = placeholder();
    public static final byte X_OK = placeholder();
    public static final byte F_OK = placeholder();

    static byte placeholder() {
        return -1;
    }

    private Unistd() {
    }

    static {
        init();
    }

    private static native void init();

    public static native void access(byte[] path, int mode) throws ErrnoException;

    public static native void symlink(byte[] target, byte[] linkpath) throws ErrnoException;

    public static native byte[] readlink(byte[] path) throws ErrnoException;

    public static native void close(int fd) throws ErrnoException;

}
