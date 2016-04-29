package linux;

public final class Unistd extends Native {

    public static final int R_OK = 4;  /* Read */
    public static final int W_OK = 2;  /* Write */
    public static final int X_OK = 1;  /* Execute */
    public static final int F_OK = 0;  /* Existence */

    private Unistd() {
    }

    public static native void access(byte[] path, int mode) throws ErrnoException;

    public static native void symlink(byte[] target, byte[] linkpath) throws ErrnoException;

    public static native byte[] readlink(byte[] path) throws ErrnoException;

    public static native void close(int fd) throws ErrnoException;

}
