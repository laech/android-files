package linux;

public final class Stat extends Native {

    static int placeholder() {
        return -1;
    }

    public static final int S_IFMT = placeholder();
    public static final int S_IFSOCK = placeholder();
    public static final int S_IFLNK = placeholder();
    public static final int S_IFREG = placeholder();
    public static final int S_IFBLK = placeholder();
    public static final int S_IFDIR = placeholder();
    public static final int S_IFCHR = placeholder();
    public static final int S_IFIFO = placeholder();
    public static final int S_ISUID = placeholder();
    public static final int S_ISGID = placeholder();
    public static final int S_ISVTX = placeholder();

    public static boolean S_ISLNK(int m) {
        return (((m) & S_IFMT) == S_IFLNK);
    }

    public static boolean S_ISREG(int m) {
        return (((m) & S_IFMT) == S_IFREG);
    }

    public static boolean S_ISDIR(int m) {
        return (((m) & S_IFMT) == S_IFDIR);
    }

    public static boolean S_ISCHR(int m) {
        return (((m) & S_IFMT) == S_IFCHR);
    }

    public static boolean S_ISBLK(int m) {
        return (((m) & S_IFMT) == S_IFBLK);
    }

    public static boolean S_ISFIFO(int m) {
        return (((m) & S_IFMT) == S_IFIFO);
    }

    public static boolean S_ISSOCK(int m) {
        return (((m) & S_IFMT) == S_IFSOCK);
    }

    public static final int S_IRWXU = placeholder();
    public static final int S_IRUSR = placeholder();
    public static final int S_IWUSR = placeholder();
    public static final int S_IXUSR = placeholder();

    public static final int S_IRWXG = placeholder();
    public static final int S_IRGRP = placeholder();
    public static final int S_IWGRP = placeholder();
    public static final int S_IXGRP = placeholder();

    public static final int S_IRWXO = placeholder();
    public static final int S_IROTH = placeholder();
    public static final int S_IWOTH = placeholder();
    public static final int S_IXOTH = placeholder();

    public int st_mode;
    public long st_size;
    public long st_mtime;
    public int st_mtime_nsec;
    public long st_blocks;

    static {
        init();
    }

    private static native void init();

    public static native void stat(byte[] path, Stat stat) throws ErrnoException;

    public static native void lstat(byte[] path, Stat stat) throws ErrnoException;

    public static native void chmod(byte[] path, int mode) throws ErrnoException;

}
