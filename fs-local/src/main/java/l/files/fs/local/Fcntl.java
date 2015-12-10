package l.files.fs.local;

import static l.files.fs.local.ErrnoException.EAGAIN;

@SuppressWarnings("OctalInteger")
final class Fcntl extends Native {

    static final int O_ACCMODE = 00000003;
    static final int O_RDONLY = 00000000;
    static final int O_WRONLY = 00000001;
    static final int O_RDWR = 00000002;
    static final int O_CREAT = 00000100;
    static final int O_EXCL = 00000200;
    static final int O_NOCTTY = 00000400;
    static final int O_TRUNC = 00001000;
    static final int O_APPEND = 00002000;
    static final int O_NONBLOCK = 00004000;
    static final int O_DSYNC = 00010000;
    static final int FASYNC = 00020000;
    static final int O_DIRECT = 00040000;
    static final int O_LARGEFILE = 00100000;
    static final int O_DIRECTORY = 00200000;
    static final int O_NOFOLLOW = 00400000;
    static final int O_NOATIME = 01000000;
    static final int O_CLOEXEC = 02000000;
    static final int __O_SYNC = 04000000;
    static final int O_SYNC = (__O_SYNC | O_DSYNC);
    static final int O_PATH = 010000000;
    static final int __O_TMPFILE = 020000000;
    static final int O_TMPFILE = (__O_TMPFILE | O_DIRECTORY);
    static final int O_TMPFILE_MASK = (__O_TMPFILE | O_DIRECTORY | O_CREAT);
    static final int O_NDELAY = O_NONBLOCK;

    private Fcntl() {
    }

    static int open(byte[] path, int flags, int mode)
            throws ErrnoException {
        while (true) {
            try {
                return nativeOpen(path, flags, mode);
            } catch (ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw e;
                }
            }
        }
    }

    private static native int nativeOpen(byte[] path, int flags, int mode)
            throws ErrnoException;

}
