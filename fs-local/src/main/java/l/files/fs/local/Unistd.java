package l.files.fs.local;

final class Unistd extends Native {

    public static final int R_OK = 4;  /* Read */
    public static final int W_OK = 2;  /* Write */
    public static final int X_OK = 1;  /* Execute */
    public static final int F_OK = 0;  /* Existence */

    private Unistd() {
    }

    static native void access(String path, int mode) throws ErrnoException;

    static native void symlink(String target, String linkpath) throws ErrnoException;

    static native String readlink(String path) throws ErrnoException;

    static native void close(int fd) throws ErrnoException;

}
