package l.files.fs.local;

import auto.parcel.AutoParcel;

/**
 * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/dirent.h.html">dirent.h</a>
 */
@AutoParcel
abstract class Dirent extends Native {

    public static final int DT_UNKNOWN = 0;
    public static final int DT_FIFO = 1;
    public static final int DT_CHR = 2;
    public static final int DT_DIR = 4;
    public static final int DT_BLK = 6;
    public static final int DT_REG = 8;
    public static final int DT_LNK = 10;
    public static final int DT_SOCK = 12;
    public static final int DT_WHT = 14;

    Dirent() {
    }

    public abstract long getInode();

    public abstract int getType();

    public abstract String getName();

    public static Dirent create(long inode, int type, String name) {
        return new AutoParcel_Dirent(inode, type, name);
    }

    static {
        init();
    }

    private static native void init();

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/opendir.html">opendir()</a>
     */
    public static native long opendir(String dir) throws ErrnoException;

    /**
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/closedir.html">closedir()</a>
     */
    public static native void closedir(long dir) throws ErrnoException;

    /**
     * Note: this will also return the "." and  ".." directories.
     * <p/>
     *
     * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/readdir.html">readdir()</a>
     */
    public static native Dirent readdir(long dir) throws ErrnoException;

}
