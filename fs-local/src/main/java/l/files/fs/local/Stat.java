package l.files.fs.local;

import android.system.ErrnoException;

import com.google.auto.value.AutoValue;

@AutoValue
@SuppressWarnings("OctalInteger")
abstract class Stat extends Native {

    Stat() {
    }

    public abstract long dev();

    public abstract long ino();

    public abstract int mode();

    public abstract long nlink();

    public abstract int uid();

    public abstract int gid();

    public abstract long rdev();

    public abstract long size();

    public abstract long atime();

    public abstract int atime_nsec();

    public abstract long mtime();

    public abstract int mtime_nsec();

    public abstract long ctime();

    public abstract int ctime_nsec();

    public abstract long blksize();

    public abstract long blocks();

    public static Stat create(
            long dev,
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
        return new AutoValue_Stat(
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

    static native Stat stat64(String path) throws ErrnoException;

    static native Stat lstat64(String path) throws ErrnoException;

}
