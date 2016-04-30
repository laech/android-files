package linux;

public final class Fcntl extends Native {

    public static final int O_ACCMODE = placeholder();
    public static final int O_RDONLY = placeholder();
    public static final int O_WRONLY = placeholder();
    public static final int O_RDWR = placeholder();
    public static final int O_CREAT = placeholder();
    public static final int O_EXCL = placeholder();
    public static final int O_NOCTTY = placeholder();
    public static final int O_TRUNC = placeholder();
    public static final int O_APPEND = placeholder();
    public static final int O_NONBLOCK = placeholder();
    public static final int O_DSYNC = placeholder();
    public static final int FASYNC = placeholder();
    public static final int O_DIRECT = placeholder();
    public static final int O_LARGEFILE = placeholder();
    public static final int O_DIRECTORY = placeholder();
    public static final int O_NOFOLLOW = placeholder();
    public static final int O_NOATIME = placeholder();
    public static final int O_CLOEXEC = placeholder();
    public static final int __O_SYNC = placeholder();
    public static final int O_SYNC = placeholder();
    public static final int O_PATH = placeholder();
    public static final int __O_TMPFILE = placeholder();
    public static final int O_TMPFILE = placeholder();
    public static final int O_TMPFILE_MASK = placeholder();
    public static final int O_NDELAY = placeholder();

    static int placeholder() {
        return -1;
    }

    private Fcntl() {
    }

    static {
        init();
    }

    private static native void init();

    public static native int open(byte[] path, int flags, int mode) throws ErrnoException;

}
