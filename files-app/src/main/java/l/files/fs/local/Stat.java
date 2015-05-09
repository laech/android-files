package l.files.fs.local;

import auto.parcel.AutoParcel;

/**
 * @see <a href="http://www.opengroup.org/onlinepubs/000095399/basedefs/sys/stat.h.html">stat.h</a>
 */
@AutoParcel
@SuppressWarnings("OctalInteger")
abstract class Stat extends Native
{

    Stat()
    {
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
            final long dev,
            final long ino,
            final int mode,
            final long nlink,
            final int uid,
            final int gid,
            final long rdev,
            final long size,
            final long atime,
            final int atime_nsec,
            final long mtime,
            final int mtime_nsec,
            final long ctime,
            final int ctime_nsec,
            final long blksize,
            final long blocks)
    {
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

    static
    {
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
