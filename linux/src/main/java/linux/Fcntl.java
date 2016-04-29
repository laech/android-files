package linux;

@SuppressWarnings("OctalInteger")
public final class Fcntl extends Native {

    public static final int O_ACCMODE = 00000003;
    public static final int O_RDONLY = 00000000;
    public static final int O_WRONLY = 00000001;
    public static final int O_RDWR = 00000002;
    public static final int O_CREAT = 00000100;
    public static final int O_EXCL = 00000200;
    public static final int O_NOCTTY = 00000400;
    public static final int O_TRUNC = 00001000;
    public static final int O_APPEND = 00002000;
    public static final int O_NONBLOCK = 00004000;
    public static final int O_DSYNC = 00010000;
    public static final int FASYNC = 00020000;
    public static final int O_DIRECT = 00040000;
    public static final int O_LARGEFILE = 00100000;
    public static final int O_DIRECTORY = 00200000;
    public static final int O_NOFOLLOW = 00400000;
    public static final int O_NOATIME = 01000000;
    public static final int O_CLOEXEC = 02000000;
    public static final int __O_SYNC = 04000000;
    public static final int O_SYNC = (__O_SYNC | O_DSYNC);
    public static final int O_PATH = 010000000;
    public static final int __O_TMPFILE = 020000000;
    public static final int O_TMPFILE = (__O_TMPFILE | O_DIRECTORY);
    public static final int O_TMPFILE_MASK = (__O_TMPFILE | O_DIRECTORY | O_CREAT);
    public static final int O_NDELAY = O_NONBLOCK;

    private Fcntl() {
    }

    public static native int open(byte[] path, int flags, int mode) throws ErrnoException;

}
