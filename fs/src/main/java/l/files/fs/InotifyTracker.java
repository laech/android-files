package l.files.fs;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import linux.ErrnoException;
import linux.Unistd;

import static l.files.base.Throwables.addSuppressed;
import static linux.Errno.EAGAIN;
import static linux.Errno.ENOSPC;
import static linux.Inotify.inotify_add_watch;
import static linux.Inotify.inotify_init;
import static linux.Inotify.inotify_rm_watch;

final class InotifyTracker {

    private static final InotifyTracker instance = new InotifyTracker();

    static InotifyTracker get() {
        return instance;
    }

    private final CopyOnWriteArrayList<WeakReference<Tracker>> trackers
            = new CopyOnWriteArrayList<>();

    private final ConcurrentMap<Integer, Entry> entries
            = new ConcurrentHashMap<>();

    private InotifyTracker() {
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

    int init(int watchLimit) throws ErrnoException {

        int fd = inotify_init();
        entries.put(fd, new Entry(watchLimit));

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

    int addWatch(int fd, byte[] path, int mask) throws ErrnoException {

        int wd;

        while (true) {
            try {

                Entry entry = entries.get(fd);
                if (entry != null && entry.watchLimit > -1 && entry.watchLimit <= entry.size()) {
                    // Throw the same error as native code to help testing.
                    throw new ErrnoException(ENOSPC);
                }

                wd = inotify_add_watch(fd, path, mask);
                break;
            } catch (ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw e;
                }
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
                inotify_rm_watch(fd, wd);
                break;
            } catch (ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw e;
                }
            }
        }
    }

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

        private final Set<Integer> wds = new HashSet<>();
        private final int watchLimit;

        private Entry(int watchLimit) {
            this.watchLimit = watchLimit;
        }

        void add(int wd) throws ErrnoException {
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

    }

    interface Tracker {

        void onInit(int fd);

        void onWatchAdded(int fd, byte[] path, int mask, int wd);

        void onWatchRemoved(int fd, int wd);

        void onClose(int fd);

    }

}
