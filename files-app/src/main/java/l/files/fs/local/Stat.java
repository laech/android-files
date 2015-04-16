package l.files.fs.local;

import auto.parcel.AutoParcel;

/**
 * @see <a href="http://www.opengroup.org/onlinepubs/000095399/basedefs/sys/stat.h.html">stat.h</a>
 */
@AutoParcel
@SuppressWarnings("OctalInteger")
abstract class Stat extends Native {

    public static final int S_IFMT = 00170000;
    public static final int S_IFSOCK = 0140000;
    public static final int S_IFLNK = 0120000;
    public static final int S_IFREG = 0100000;
    public static final int S_IFBLK = 0060000;
    public static final int S_IFDIR = 0040000;
    public static final int S_IFCHR = 0020000;
    public static final int S_IFIFO = 0010000;
    public static final int S_ISUID = 0004000;
    public static final int S_ISGID = 0002000;
    public static final int S_ISVTX = 0001000;

    public static boolean S_ISLNK(int m) { return (((m) & S_IFMT) == S_IFLNK); }
    public static boolean S_ISREG(int m) { return (((m) & S_IFMT) == S_IFREG); }
    public static boolean S_ISDIR(int m) { return (((m) & S_IFMT) == S_IFDIR); }
    public static boolean S_ISCHR(int m) { return (((m) & S_IFMT) == S_IFCHR); }
    public static boolean S_ISBLK(int m) { return (((m) & S_IFMT) == S_IFBLK); }
    public static boolean S_ISFIFO(int m) { return (((m) & S_IFMT) == S_IFIFO); }
    public static boolean S_ISSOCK(int m) { return (((m) & S_IFMT) == S_IFSOCK); }

    public static final int S_IRWXU = 00700;
    public static final int S_IRUSR = 00400;
    public static final int S_IWUSR = 00200;
    public static final int S_IXUSR = 00100;

    public static final int S_IRWXG = 00070;
    public static final int S_IRGRP = 00040;
    public static final int S_IWGRP = 00020;
    public static final int S_IXGRP = 00010;

    public static final int S_IRWXO = 00007;
    public static final int S_IROTH = 00004;
    public static final int S_IWOTH = 00002;
    public static final int S_IXOTH = 00001;

    Stat() {
    }

    public abstract long getDev();

    public abstract long getIno();

    public abstract int getMode();

    public abstract long getNlink();

    public abstract int getUid();

    public abstract int getGid();

    public abstract long getRdev();

    public abstract long getSize();

    public abstract long getAtime();

    public abstract int getAtimeNsec();

    public abstract long getMtime();

    public abstract int getMtimeNsec();

    public abstract long getCtime();

    public abstract int getCtimeNsec();

    public abstract long getBlksize();

    public abstract long getBlocks();

    public static Stat create(long dev,
                              long ino,
                              int mode,
                              long nlink,
                              int uid,
                              int gid,
                              long rdev,
                              long size,
                              long atime,
                              int atime_nsec,
                              long mtime,
                              int mtime_nsec,
                              long ctime,
                              int ctime_nsec,
                              long blksize,
                              long blocks) {
        return new AutoParcel_Stat(
                dev,
                ino,
                mode,
                nlink,
                uid,
                gid,
                rdev,
                size,
                atime,
                atime_nsec,
                mtime,
                mtime_nsec,
                ctime,
                ctime_nsec,
                blksize,
                blocks);
    }

    static {
        init();
    }

    private static native void init();

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/stat.html">stat()</a>
     */
    public static native Stat stat(String path) throws ErrnoException;

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/lstat.html">lstat()</a>
     */
    public static native Stat lstat(String path) throws ErrnoException;

}
