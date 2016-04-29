package linux;

public final class Dirent extends Native {

    public static final byte DT_UNKNOWN = 0;
    public static final byte DT_FIFO = 1;
    public static final byte DT_CHR = 2;
    public static final byte DT_DIR = 4;
    public static final byte DT_BLK = 6;
    public static final byte DT_REG = 8;
    public static final byte DT_LNK = 10;
    public static final byte DT_SOCK = 12;
    public static final byte DT_WHT = 14;

    public long d_ino;
    public byte d_type;

    /**
     * Only valid from 0 (inclusive) to {@link #d_name_len} (exclusive).
     */
    public byte[] d_name = new byte[256]; // 256 = NAME_MAX, see dirent.h

    /**
     * The length of the name (not in dirent.h).
     */
    public int d_name_len;

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
