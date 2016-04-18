package l.files.fs.local;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static l.files.base.Objects.requireNonNull;
import static l.files.base.Throwables.addSuppressed;
import static linux.Errno.EAGAIN;
import static linux.Errno.ENOMEM;
import static linux.Errno.ENOSPC;

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

    // static final int IN_NONBLOCK = OsConstants.O_NONBLOCK;
    // static final int IN_CLOEXEC = OsConstants.FD_CLOEXEC;

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

    private static final Inotify instance = new Inotify();

    static Inotify get() {
        return instance;
    }

    private final CopyOnWriteArrayList<WeakReference<Tracker>> trackers
            = new CopyOnWriteArrayList<>();

    private final ConcurrentMap<Integer, Entry> entries
            = new ConcurrentHashMap<>();

    private Inotify() {
    }

    Tracker registerTracker(Tracker tracker) {
        trackers.add(new WeakReference<>(tracker));
        return tracker;
    }

    void unregisterTracker(Tracker tracker) {
        for (WeakReference<Tracker> ref : trackers) {
            Tracker current = ref.get();
            if (current == null || tracker == current) {
                trackers.remove(ref);
            }
        }
    }

    private boolean makeRoomFor(int fd) {
        Entry largest = null;
        for (Entry entry : entries.values()) {
            if (largest == null || largest.size() < entry.size()) {
                largest = entry;
            }
        }
        if (largest != null) {
            largest.release();
            return largest.fd() != fd;
        }
        return false;
    }

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_init.2.html">inotify_init()</a>
     */
    int init(Callback obj) throws ErrnoException {

        int fd = internalInit();
        entries.put(fd, new Entry(fd, obj));

        try {

            notifyInit(fd);

        } catch (Throwable e) {
            try {
                close(fd);
            } catch (Throwable sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }

        return fd;
    }

    private static native int internalInit() throws ErrnoException;

    private void notifyInit(int fd) {
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

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_add_watch.2.html">inotify_add_watch()</a>
     */
    int addWatch(int fd, byte[] path, int mask) throws ErrnoException {

        int wd;
        try {

            while (true) {
                try {
                    wd = internalAddWatch(fd, path, mask);
                    break;
                } catch (ErrnoException e) {
                    if (e.errno != EAGAIN) {
                        throw e;
                    }
                }
            }

        } catch (ErrnoException e) {

            if (e.errno == ENOSPC || e.errno == ENOMEM) {

                if (!makeRoomFor(fd)) {
                    throw e;
                }
                wd = internalAddWatchRetry(fd, path, mask);

            } else {
                throw e;
            }
        }

        Entry entry = entries.get(fd);
        if (entry != null) {
            entry.add(wd);
        }

        try {

            notifyAddWatch(fd, path, mask, wd);

        } catch (Throwable e) {
            try {
                removeWatch(fd, wd);
            } catch (Throwable sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }

        return wd;
    }

    private int internalAddWatchRetry(int fd, byte[] path, int mask) throws ErrnoException {
        while (true) {
            try {
                return internalAddWatch(fd, path, mask);
            } catch (ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw e;
                }
            }
        }
    }

    private void notifyAddWatch(int fd, byte[] path, int mask, int wd) {
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

    private static native int internalAddWatch(int fd, byte[] path, int mask) throws ErrnoException;

    /**
     * @see <a href="http://man7.org/linux/man-pages/man2/inotify_rm_watch.2.html">inotify_rm_watch()</a>
     */
    void removeWatch(int fd, int wd) throws ErrnoException {
        internalRemoveWatchRetry(fd, wd);

        Entry entry = entries.get(fd);
        if (entry != null) {
            entry.remove(wd);
        }

        notifyRemoveWatch(fd, wd);
    }

    private void internalRemoveWatchRetry(int fd, int wd) throws ErrnoException {
        while (true) {
            try {
                internalRemoveWatch(fd, wd);
                break;
            } catch (ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw e;
                }
            }
        }
    }

    private static native void internalRemoveWatch(int fd, int wd) throws ErrnoException;

    private void notifyRemoveWatch(int fd, int wd) {
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

    void close(int fd) throws ErrnoException {
        Unistd.close(fd);
        entries.remove(fd);
        notifyClose(fd);
    }

    private void notifyClose(int fd) {
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

    private final class Entry {

        private final int fd;
        private final Set<Integer> wds = new HashSet<>();
        private final Callback callback;

        private Entry(int fd, Callback callback) {
            this.fd = fd;
            this.callback = requireNonNull(callback);
        }

        int fd() {
            return fd;
        }

        void add(int wd) {
            synchronized (this) {
                wds.add(wd);
            }
        }

        void remove(int wd) {
            synchronized (this) {
                wds.remove(wd);
            }
        }

        int size() {
            synchronized (this) {
                return wds.size();
            }
        }

        void release() {

            Set<Integer> copy;
            synchronized (this) {
                copy = new HashSet<>(wds);
                wds.clear();
            }

            if (copy.isEmpty()) {
                return;
            }

            for (int wd : copy) {
                try {
                    removeWatch(fd, wd);
                } catch (ErrnoException ignored) {
                }
            }

            callback.onWatchesReleased(copy);
        }

    }

    interface Callback {

        /**
         * Called when this instance's watches have been removed
         * forcibly due to system limits.
         */
        void onWatchesReleased(Set<Integer> wds);

    }

    interface Tracker {

        void onInit(int fd);

        void onWatchAdded(int fd, byte[] path, int mask, int wd);

        void onWatchRemoved(int fd, int wd);

        void onClose(int fd);

    }

}
