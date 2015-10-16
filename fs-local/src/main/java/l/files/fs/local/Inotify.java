package l.files.fs.local;

import android.system.ErrnoException;
import android.system.OsConstants;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @see <a href="http://man7.org/linux/man-pages/man7/inotify.7.html">inotify</a>
 */
final class Inotify extends Native {

    static final int IN_ACCESS = 0x00000001;
    static final int IN_MODIFY = 0x00000002;
    static final int IN_ATTRIB = 0x00000004;
    static final int IN_CLOSE_WRITE = 0x00000008;
    static final int IN_CLOSE_NOWRITE = 0x00000010;
    static final int IN_OPEN = 0x00000020;
    static final int IN_MOVED_FROM = 0x00000040;
    static final int IN_MOVED_TO = 0x00000080;
    static final int IN_CREATE = 0x00000100;
    static final int IN_DELETE = 0x00000200;
    static final int IN_DELETE_SELF = 0x00000400;
    static final int IN_MOVE_SELF = 0x00000800;

    static final int IN_UNMOUNT = 0x00002000;
    static final int IN_Q_OVERFLOW = 0x00004000;
    static final int IN_IGNORED = 0x00008000;

    static final int IN_CLOSE = IN_CLOSE_WRITE | IN_CLOSE_NOWRITE;
    static final int IN_MOVE = IN_MOVED_FROM | IN_MOVED_TO;

    static final int IN_ONLYDIR = 0x01000000;
    static final int IN_DONT_FOLLOW = 0x02000000;
    static final int IN_EXCL_UNLINK = 0x04000000;
    static final int IN_MASK_ADD = 0x20000000;
    static final int IN_ISDIR = 0x40000000;
    static final int IN_ONESHOT = 0x80000000;

    static final int IN_NONBLOCK = OsConstants.O_NONBLOCK;
    static final int IN_CLOEXEC = OsConstants.FD_CLOEXEC;

    static final int IN_ALL_EVENTS
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

    private static final CopyOnWriteArrayList<WeakReference<Tracker>> trackers
            = new CopyOnWriteArrayList<>();

    private Inotify() {
    }

    static Tracker registerTracker(Tracker tracker) {
        trackers.add(new WeakReference<>(tracker));
        return tracker;
    }

    static void unregisterTracker(Tracker tracker) {
        for (WeakReference<Tracker> ref : trackers) {
            Tracker current = ref.get();
            if (current == null || tracker == current) {
                trackers.remove(ref);
            }
        }
    }

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_init.2.html">inotify_init()</a>
     */
    static int init() throws ErrnoException {
        int fd = internalInit();
        notifyInit(fd);
        return fd;
    }

    private static native int internalInit() throws ErrnoException;

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_init.2.html">inotify_init1()</a>
     */
    static int init1(int flags) throws ErrnoException {
        int fd = internalInit1(flags);
        notifyInit(fd);
        return fd;
    }

    private static void notifyInit(int fd) {
        if (trackers.isEmpty()) {
            return;
        }

        for (WeakReference<Tracker> ref : trackers) {
            Tracker tracker = ref.get();
            if (tracker != null) {
                tracker.onInit(fd);
            } else {
                // CopyOnWriteArrayList okay to remove while iterating
                trackers.remove(ref);
            }
        }
    }

    private static native int internalInit1(int flags) throws ErrnoException;

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_add_watch.2.html">inotify_add_watch()</a>
     */
    static int addWatch(int fd, String path, int mask) throws ErrnoException {
        int wd = internalAddWatch(fd, path, mask);
        notifyAddWatch(fd, path, mask, wd);
        return wd;
    }

    private static void notifyAddWatch(int fd, String path, int mask, int wd) {
        if (trackers.isEmpty()) {
            return;
        }
        for (WeakReference<Tracker> ref : trackers) {
            Tracker tracker = ref.get();
            if (tracker != null) {
                tracker.onWatchAdded(fd, path, mask, wd);
            } else {
                // CopyOnWriteArrayList okay to remove while iterating
                trackers.remove(ref);
            }
        }
    }

    private static native int internalAddWatch(int fd, String path, int mask) throws ErrnoException;

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_rm_watch.2.html">inotify_rm_watch()</a>
     */
    static void removeWatch(int fd, int wd) throws ErrnoException {
        internalRemoveWatch(fd, wd);
        notifyRemoveWatch(fd, wd);
    }

    private static native void internalRemoveWatch(int fd, int wd) throws ErrnoException;

    private static void notifyRemoveWatch(int fd, int wd) {
        if (trackers.isEmpty()) {
            return;
        }
        for (WeakReference<Tracker> ref : trackers) {
            Tracker tracker = ref.get();
            if (tracker != null) {
                tracker.onWatchRemoved(fd, wd);
            } else {
                // CopyOnWriteArrayList okay to remove while iterating
                trackers.remove(ref);
            }
        }
    }

    static void close(int fd) throws ErrnoException {
        Unistd.close(fd);
        notifyClose(fd);
    }

    private static void notifyClose(int fd) {
        if (trackers.isEmpty()) {
            return;
        }
        for (WeakReference<Tracker> ref : trackers) {
            Tracker tracker = ref.get();
            if (tracker != null) {
                tracker.onClose(fd);
            } else {
                // CopyOnWriteArrayList okay to remove while iterating
                trackers.remove(ref);
            }
        }
    }

    public static class Tracker implements Closeable {

        public void onInit(int fd) {
        }

        public void onWatchAdded(int fd, String path, int mask, int wd) {
        }

        public void onWatchRemoved(int fd, int wd) {
        }

        public void onClose(int fd) {
        }

        @Override
        public void close() throws IOException {
            unregisterTracker(this);
        }
    }

}
