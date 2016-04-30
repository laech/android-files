package linux;

public final class Vfs {

    private Vfs() {
    }

    public static final long ADFS_SUPER_MAGIC = placeholder();
    public static final long AFFS_SUPER_MAGIC = placeholder();
    public static final long BEFS_SUPER_MAGIC = placeholder();
    public static final long BFS_MAGIC = placeholder();
    public static final long CIFS_MAGIC_NUMBER = placeholder();
    public static final long CODA_SUPER_MAGIC = placeholder();
    public static final long COH_SUPER_MAGIC = placeholder();
    public static final long CRAMFS_MAGIC = placeholder();
    public static final long DEVFS_SUPER_MAGIC = placeholder();
    public static final long EFS_SUPER_MAGIC = placeholder();
    public static final long EXT_SUPER_MAGIC = placeholder();
    public static final long EXT2_OLD_SUPER_MAGIC = placeholder();
    public static final long EXT2_SUPER_MAGIC = placeholder();
    public static final long EXT3_SUPER_MAGIC = placeholder();
    public static final long HFS_SUPER_MAGIC = placeholder();
    public static final long HPFS_SUPER_MAGIC = placeholder();
    public static final long HUGETLBFS_MAGIC = placeholder();
    public static final long ISOFS_SUPER_MAGIC = placeholder();
    public static final long JFFS2_SUPER_MAGIC = placeholder();
    public static final long JFS_SUPER_MAGIC = placeholder();
    public static final long MINIX_SUPER_MAGIC = placeholder();
    public static final long MINIX_SUPER_MAGIC2 = placeholder();
    public static final long MINIX2_SUPER_MAGIC = placeholder();
    public static final long MINIX2_SUPER_MAGIC2 = placeholder();
    public static final long MSDOS_SUPER_MAGIC = placeholder();
    public static final long NCP_SUPER_MAGIC = placeholder();
    public static final long NFS_SUPER_MAGIC = placeholder();
    public static final long NTFS_SB_MAGIC = placeholder();
    public static final long OPENPROM_SUPER_MAGIC = placeholder();
    public static final long PROC_SUPER_MAGIC = placeholder();
    public static final long QNX4_SUPER_MAGIC = placeholder();
    public static final long REISERFS_SUPER_MAGIC = placeholder();
    public static final long ROMFS_MAGIC = placeholder();
    public static final long SMB_SUPER_MAGIC = placeholder();
    public static final long SYSV2_SUPER_MAGIC = placeholder();
    public static final long SYSV4_SUPER_MAGIC = placeholder();
    public static final long TMPFS_MAGIC = placeholder();
    public static final long UDF_SUPER_MAGIC = placeholder();
    public static final long UFS_MAGIC = placeholder();
    public static final long USBDEVICE_SUPER_MAGIC = placeholder();
    public static final long VXFS_SUPER_MAGIC = placeholder();
    public static final long XENIX_SUPER_MAGIC = placeholder();
    public static final long XFS_SUPER_MAGIC = placeholder();
    public static final long _XIAFS_SUPER_MAGIC = placeholder();

    static long placeholder() {
        return -1;
    }

    static {
        init();
    }

    private static native void init();

    public static native void statfs(byte[] path, Statfs statfs) throws ErrnoException;

    public static final class Statfs {
        public long f_type = placeholder();
        public long f_bsize = placeholder();
        public long f_blocks = placeholder();
        public long f_bfree = placeholder();
        public long f_bavail = placeholder();
        public long f_files = placeholder();
        public long f_ffree = placeholder();
        public long f_namelen = placeholder();
        public long f_frsize = placeholder();
        public long f_flags = placeholder();
    }
}
