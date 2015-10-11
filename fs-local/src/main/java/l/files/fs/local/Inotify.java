package l.files.fs.local;

import android.system.ErrnoException;
import android.system.OsConstants;

/**
 * @see <a href="http://man7.org/linux/man-pages/man7/inotify.7.html">inotify</a>
 */
final class Inotify extends Native {

    public static final int IN_ACCESS = 0x00000001;
    public static final int IN_MODIFY = 0x00000002;
    public static final int IN_ATTRIB = 0x00000004;
    public static final int IN_CLOSE_WRITE = 0x00000008;
    public static final int IN_CLOSE_NOWRITE = 0x00000010;
    public static final int IN_OPEN = 0x00000020;
    public static final int IN_MOVED_FROM = 0x00000040;
    public static final int IN_MOVED_TO = 0x00000080;
    public static final int IN_CREATE = 0x00000100;
    public static final int IN_DELETE = 0x00000200;
    public static final int IN_DELETE_SELF = 0x00000400;
    public static final int IN_MOVE_SELF = 0x00000800;

    public static final int IN_UNMOUNT = 0x00002000;
    public static final int IN_Q_OVERFLOW = 0x00004000;
    public static final int IN_IGNORED = 0x00008000;

    public static final int IN_CLOSE = IN_CLOSE_WRITE | IN_CLOSE_NOWRITE;
    public static final int IN_MOVE = IN_MOVED_FROM | IN_MOVED_TO;

    public static final int IN_ONLYDIR = 0x01000000;
    public static final int IN_DONT_FOLLOW = 0x02000000;
    public static final int IN_EXCL_UNLINK = 0x04000000;
    public static final int IN_MASK_ADD = 0x20000000;
    public static final int IN_ISDIR = 0x40000000;
    public static final int IN_ONESHOT = 0x80000000;

    public static final int IN_NONBLOCK = OsConstants.O_NONBLOCK;
    public static final int IN_CLOEXEC = OsConstants.FD_CLOEXEC;

    public static final int IN_ALL_EVENTS
            = IN_ACCESS
            | IN_MODIFY
            | IN_ATTRIB
            | IN_CLOSE_WRITE
            | IN_CLOSE_NOWRITE
            | IN_OPEN
            | IN_MOVED_FROM
            | IN_MOVED_TO
            | IN_DELETE
            | IN_CREATE
            | IN_DELETE_SELF
            | IN_MOVE_SELF;

    private Inotify() {
    }

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_init.2.html">inotify_init()</a>
     */
    public static native int init() throws ErrnoException;

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_init.2.html">inotify_init1()</a>
     */
    public static native int init1(int flags) throws ErrnoException;

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_add_watch.2.html">inotify_add_watch()</a>
     */
    public static native int addWatch(int fd, String path, int mask) throws ErrnoException;

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_rm_watch.2.html">inotify_rm_watch()</a>
     */
    public static native void removeWatch(int fd, int wd) throws ErrnoException;

}