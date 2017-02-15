package linux;

import l.files.fs.Native;

public final class Inotify extends Native {

    static int placeholder() {
        return -1;
    }

    public static final int IN_ACCESS = placeholder();
    public static final int IN_MODIFY = placeholder();
    public static final int IN_ATTRIB = placeholder();
    public static final int IN_CLOSE_WRITE = placeholder();
    public static final int IN_CLOSE_NOWRITE = placeholder();
    public static final int IN_OPEN = placeholder();
    public static final int IN_MOVED_FROM = placeholder();
    public static final int IN_MOVED_TO = placeholder();
    public static final int IN_CREATE = placeholder();
    public static final int IN_DELETE = placeholder();
    public static final int IN_DELETE_SELF = placeholder();
    public static final int IN_MOVE_SELF = placeholder();

    public static final int IN_UNMOUNT = placeholder();
    public static final int IN_Q_OVERFLOW = placeholder();
    public static final int IN_IGNORED = placeholder();

    public static final int IN_CLOSE = placeholder();
    public static final int IN_MOVE = placeholder();

    public static final int IN_ONLYDIR = placeholder();
    public static final int IN_DONT_FOLLOW = placeholder();
    public static final int IN_MASK_ADD = placeholder();
    public static final int IN_ISDIR = placeholder();
    public static final int IN_ONESHOT = placeholder();

    public static final int IN_ALL_EVENTS = placeholder();

    private Inotify() {
    }

    static {
        init();
    }

    private static native void init();

    public static native int inotify_init() throws ErrnoException;

    public static native int inotify_add_watch(int fd, byte[] path, int mask) throws ErrnoException;

    public static native void inotify_rm_watch(int fd, int wd) throws ErrnoException;

}
