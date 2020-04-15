package linux;

import l.files.fs.Native;
import l.files.fs.PathType;

import static linux.Limits.NAME_MAX;

public final class Dirent extends Native {

    public static final byte DT_UNKNOWN = placeholder();
    public static final byte DT_FIFO = placeholder();
    public static final byte DT_CHR = placeholder();
    public static final byte DT_DIR = placeholder();
    public static final byte DT_BLK = placeholder();
    public static final byte DT_REG = placeholder();
    public static final byte DT_LNK = placeholder();
    public static final byte DT_SOCK = placeholder();
    public static final byte DT_WHT = placeholder();

    static byte placeholder() {
        return -1;
    }

    public long d_ino;
    public byte d_type;

    /**
     * Only valid from 0 (inclusive) to {@link #d_name_len} (exclusive).
     */
    public byte[] d_name = new byte[NAME_MAX];

    /**
     * The length of the name (not in dirent.h).
     */
    public int d_name_len;

    static {
        init();
    }

    public PathType type() {
        if (d_type == DT_REG) {
            return PathType.FILE;
        }
        if (d_type == DT_DIR) {
            return PathType.DIRECTORY;
        }
        if (d_type == DT_LNK) {
            return PathType.SYMLINK;
        }
        return PathType.OTHER;
    }

    public boolean isSelfOrParent() {
        int len = d_name_len;
        byte[] name = d_name;
        return (len == 1 && name[0] == '.') ||
            (len == 2 && name[0] == '.' && name[1] == '.');
    }

    private static native void init();

    public static native DIR opendir(byte[] path) throws ErrnoException;

    public static native void closedir(DIR dir) throws ErrnoException;

    public static native Dirent readdir(DIR dir, Dirent entry) throws ErrnoException;

    public static final class DIR {

        private final long address;
        private boolean closed;

        DIR(long address) {
            this.address = address;
        }

    }

}
