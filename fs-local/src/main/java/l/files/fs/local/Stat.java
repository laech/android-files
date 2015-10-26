package l.files.fs.local;

import com.google.auto.value.AutoValue;

@AutoValue
@SuppressWarnings("OctalInteger")
abstract class Stat extends Native {

    static final int S_IFMT = 00170000;
    static final int S_IFSOCK = 0140000;
    static final int S_IFLNK = 0120000;
    static final int S_IFREG = 0100000;
    static final int S_IFBLK = 0060000;
    static final int S_IFDIR = 0040000;
    static final int S_IFCHR = 0020000;
    static final int S_IFIFO = 0010000;
    static final int S_ISUID = 0004000;
    static final int S_ISGID = 0002000;
    static final int S_ISVTX = 0001000;

    static boolean S_ISLNK(int m) {
        return (((m) & S_IFMT) == S_IFLNK);
    }

    static boolean S_ISREG(int m) {
        return (((m) & S_IFMT) == S_IFREG);
    }

    static boolean S_ISDIR(int m) {
        return (((m) & S_IFMT) == S_IFDIR);
    }

    static boolean S_ISCHR(int m) {
        return (((m) & S_IFMT) == S_IFCHR);
    }

    static boolean S_ISBLK(int m) {
        return (((m) & S_IFMT) == S_IFBLK);
    }

    static boolean S_ISFIFO(int m) {
        return (((m) & S_IFMT) == S_IFIFO);
    }

    static boolean S_ISSOCK(int m) {
        return (((m) & S_IFMT) == S_IFSOCK);
    }

    static final int S_IRWXU = 00700;
    static final int S_IRUSR = 00400;
    static final int S_IWUSR = 00200;
    static final int S_IXUSR = 00100;

    static final int S_IRWXG = 00070;
    static final int S_IRGRP = 00040;
    static final int S_IWGRP = 00020;
    static final int S_IXGRP = 00010;

    static final int S_IRWXO = 00007;
    static final int S_IROTH = 00004;
    static final int S_IWOTH = 00002;
    static final int S_IXOTH = 00001;

    Stat() {
    }

    abstract long dev();

    abstract long ino();

    abstract int mode();

    abstract long nlink();

    abstract int uid();

    abstract int gid();

    abstract long rdev();

    abstract long size();

    abstract long mtime();

    abstract int mtime_nsec();

    abstract long ctime();

    abstract int ctime_nsec();

    abstract long blksize();

    abstract long blocks();

    static Stat create(
            long dev,
            long ino,
            int mode,
            long nlink,
            int uid,
            int gid,
            long rdev,
            long size,
            long mtime,
            int mtime_nsec,
            long ctime,
            int ctime_nsec,
            long blksize,
            long blocks) {
        return new AutoValue_Stat(
                dev,
                ino,
                mode,
                nlink,
                uid,
                gid,
                rdev,
                size,
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

    static native Stat stat(String path) throws ErrnoException;

    static native Stat lstat(String path) throws ErrnoException;

    static native Stat fstat(int fd) throws ErrnoException;

    static native void chmod(String path, int mode) throws ErrnoException;

    static native void mkdir(String path, int mode) throws ErrnoException;

}
