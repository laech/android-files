package l.files.fs.local;

import android.os.Handler;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import l.files.fs.Event;
import l.files.fs.FileSystem.Consumer;
import l.files.fs.Files;
import l.files.fs.LinkOption;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Path;
import linux.Dirent;
import linux.Dirent.DIR;
import linux.ErrnoException;
import linux.Fcntl;
import linux.Vfs.Statfs;

import static android.os.Looper.getMainLooper;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Thread.currentThread;
import static l.files.base.Objects.requireNonNull;
import static l.files.base.Throwables.addSuppressed;
import static l.files.fs.Event.CREATE;
import static l.files.fs.Event.DELETE;
import static l.files.fs.Event.MODIFY;
import static l.files.fs.Files.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.LocalFileSystem.isSelfOrParent;
import static linux.Errno.EACCES;
import static linux.Errno.EINVAL;
import static linux.Errno.ENOENT;
import static linux.Errno.ENOMEM;
import static linux.Errno.ENOSPC;
import static linux.Fcntl.O_DIRECTORY;
import static linux.Fcntl.O_NOFOLLOW;
import static linux.Inotify.IN_ACCESS;
import static linux.Inotify.IN_ATTRIB;
import static linux.Inotify.IN_CLOSE_NOWRITE;
import static linux.Inotify.IN_CLOSE_WRITE;
import static linux.Inotify.IN_CREATE;
import static linux.Inotify.IN_DELETE;
import static linux.Inotify.IN_DELETE_SELF;
import static linux.Inotify.IN_DONT_FOLLOW;
import static linux.Inotify.IN_IGNORED;
import static linux.Inotify.IN_ISDIR;
import static linux.Inotify.IN_MODIFY;
import static linux.Inotify.IN_MOVED_FROM;
import static linux.Inotify.IN_MOVED_TO;
import static linux.Inotify.IN_MOVE_SELF;
import static linux.Inotify.IN_ONLYDIR;
import static linux.Inotify.IN_OPEN;
import static linux.Inotify.IN_Q_OVERFLOW;
import static linux.Inotify.IN_UNMOUNT;
import static linux.Vfs.PROC_SUPER_MAGIC;
import static linux.Vfs.statfs;

final class LocalObservable extends Native
        implements Runnable, Observation {

    /*
     * This classes uses inotify for monitoring file system events. This
     * differs with android.os.FileObserver:
     *
     *  - Each instance of this classes will have its own inotify instance.
     *  - FileObserver has a single global inotify shared by all instances.
     *
     * Having a single global inotify instance has been problematic in the past
     * in the follow ways:
     *
     *  - Starting a new instance of FileObserver to watch a path that is
     *    currently being watched by another instance will cause the event mask
     *    to change for the other instance since they belong to the same
     *    inotify instance.
     *
     *  - Similar to the above, stopping an instance of FileObserver for a path
     *    will cause all other instances in other parts of the system that are
     *    watching on the same path to be stopped as well.
     *
     *  - Since inotify really operates on inodes instead of paths, the above
     *    two cases can happen for different paths, i.e. two FileObserver
     *    instances for different paths can interfere with each other if the
     *    paths are actually pointing to the same inode, for example
     *    /storage/emulated/0 and /storage/emulated/legacy are pointing to the
     *    same inode (as of this writing) and they are not symlinks.
     *
     *  - There are also other challenges if one chooses to keep track of all
     *    FileObserver instances which will make the code complex and more
     *    difficult to maintain and test.
     *
     *  - This class also tracks all child directories of the main directory
     *    being watched, but with a different mask, and have them disposed when
     *    the main directory is no longer being watched. This will be difficult
     *    to achieve because of the above.
     *
     * So that's why this class exists, every instance will be isolated and not
     * interfere with anything, but it does mean that more inotify instances
     * will be required and they are of a limited resource.
     */

    /*
     * No last access time watch because it is unreliable and it's not needed
     */

    /*
     * New bug affecting Android M (API 23) inotify, meaning some events will
     * not be delivered.
     *
     * Examples:
     *
     *  - File download via DownloadManager
     *  - 'touch file' using adb shell
     *
     * Issues:
     *
     *  - https://code.google.com/p/android/issues/detail?id=189231
     *  - https://code.google.com/p/android-developer-preview/issues/detail?id=3099
     */

    private static final Handler handler = new Handler(getMainLooper());

    /**
     * Mask to use for root file.
     */
    private static final int ROOT_MASK
            = IN_ATTRIB
            | IN_CREATE
            | IN_DELETE
            | IN_DELETE_SELF
            | IN_MODIFY
            | IN_MOVE_SELF
            | IN_MOVED_FROM
            | IN_MOVED_TO;

    /**
     * Mask to use for immediate sub directories of root directory. Only care
     * about events that will change the attributes of the sub directory but not
     * reported through {@link #ROOT_MASK}.
     */
    private static final int CHILD_DIR_MASK
            = IN_DONT_FOLLOW
            | IN_ONLYDIR
            | IN_CREATE
            | IN_DELETE
            | IN_MOVED_FROM
            | IN_MOVED_TO;

    static {
        init();
    }

    private static native void init();

    private static boolean isProcfs(byte[] path) throws ErrnoException {
        Statfs statfs = new Statfs();
        statfs(path, statfs);
        return statfs.f_type == PROC_SUPER_MAGIC;
    }

    private final InotifyTracker inotify = InotifyTracker.get();

    private volatile int fd = -1;
    private volatile int wd = -1;

    private final LocalPath root;

    private final String tag;

    /**
     * If {@link #root} is a directory, its immediate child directories will
     * also be watched since creation of files inside a child directory will
     * update the child directory's attributes, and that update won't be
     * reported by other events. This is a map of watch descriptors to the child
     * directories name being watched, and it will be updated as directories are
     * being created/deleted.
     */
    private final ConcurrentBiMap<Integer, LocalName> childDirs = new ConcurrentBiMap<>();

    private final WeakReference<Observer> observerRef;

    /**
     * Background thread that polls for events.
     */
    private final AtomicReference<Thread> thread;

    private final AtomicBoolean closed;
    private final AtomicBoolean released;

    LocalObservable(LocalPath root, Observer observer) {
        this(root, observer, null);
    }

    LocalObservable(LocalPath root, Observer observer, String tag) {
        this.root = requireNonNull(root);
        this.observerRef = new WeakReference<>(requireNonNull(observer));
        this.thread = new AtomicReference<>(null);
        this.closed = new AtomicBoolean(false);
        this.released = new AtomicBoolean(false);
        this.tag = tag;
    }

    void start(LinkOption option, Consumer<? super Path> childrenConsumer, int watchLimit)
            throws IOException, InterruptedException {

        requireNonNull(option);
        requireNonNull(childrenConsumer);

        try {
            if (!isProcfs(root.path)) {
                fd = inotify.init(watchLimit);
                wd = inotifyAddWatchWillCloseOnError(option);
            } else {
                close();
            }
        } catch (ErrnoException e) {
            fd = -1;
            wd = -1;
            close();
            notifyIncompleteObservationOrClose();
            return;
        }

        /* Using a new thread seems to be much quicker than using a thread pool
         * executor, didn't investigate why, just a reminder here to check the
         * performance if this is to be changed to a pool.
         */
        Thread thread = null;
        try {

            /*
             * Start the thread then observe on the children directories, this
             * allows them to happen at the same time, just need to make sure the
             * latest one wins.
             */
            if (fd != -1) {
                thread = new Thread(this);
                thread.setName(toString());
                thread.start();
            }

            if (Files.stat(root, option).isDirectory()) {
                traverseChildren(option, childrenConsumer);
            }

        } catch (Throwable e) {

            if (thread != null) {
                thread.interrupt();
            }
            try {
                close();
            } catch (Exception sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }

    }

    private int inotifyAddWatchWillCloseOnError(LinkOption opt) throws ErrnoException {

        try {

            int mask = ROOT_MASK | (opt == NOFOLLOW ? IN_DONT_FOLLOW : 0);
            return inotify.addWatch(fd, root.path, mask);

        } catch (Throwable e) {
            try {
                inotify.close(fd);
            } catch (ErrnoException sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }
    }

    private void traverseChildren(
            LinkOption option,
            Consumer<? super Path> childrenConsumer)
            throws IOException, InterruptedException {

        boolean limitReached = fd == -1;

        int flags = O_DIRECTORY;
        if (option == NOFOLLOW) {
            flags |= O_NOFOLLOW;
        }

        try {
            DIR dir = Dirent.fdopendir(Fcntl.open(root.path, flags, 0));
            try {
                Dirent entry = new Dirent();
                while (!isClosed() && (entry = Dirent.readdir(dir, entry)) != null) {

                    if (isSelfOrParent(entry)) {
                        continue;
                    }

                    byte[] name = Arrays.copyOf(entry.d_name, entry.d_name_len);
                    LocalPath child = root.resolve(name);
                    if (!childrenConsumer.accept(child)) {
                        currentThread().interrupt();
                        break;
                    }

                    if (limitReached || entry.d_type != Dirent.DT_DIR || released.get()) {
                        continue;
                    }

                    try {

                        byte[] childPath = child.path;
                        int wd = inotify.addWatch(fd, childPath, CHILD_DIR_MASK);
                        childDirs.put(wd, LocalName.wrap(name));

                    } catch (ErrnoException e) {
                        handleAddChildDirWatchFailure(e);
                    }

                    if (currentThread().isInterrupted()) {
                        break;
                    }
                }
            } finally {
                Dirent.closedir(dir);
            }
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, root);
        }

        if (currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{fd=" + fd + ",wd=" + wd + ",root=" + root + "}";
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void close() throws IOException {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        Thread t = thread.get();
        if (t != null) {
            t.interrupt();
        }

        if (fd == -1) {
            return;
        }

        List<ErrnoException> suppressed = new ArrayList<>(0);
        for (Integer wd : childDirs.keySet()) {
            try {
                inotify.removeWatch(fd, wd);
            } catch (ErrnoException e) {
                if (e.errno != EINVAL) {
                    suppressed.add(e);
                }
            }
        }

        if (wd != -1) {
            try {
                inotify.removeWatch(fd, wd);
            } catch (ErrnoException e) {
                if (e.errno != EINVAL) {
                    suppressed.add(e);
                }
            }
        }

        try {

            inotify.close(fd);

        } catch (ErrnoException e) {
            for (ErrnoException sup : suppressed) {
                addSuppressed(e, sup);
            }
            throw ErrnoExceptions.toIOException(e, root);
        }

        if (!suppressed.isEmpty()) {
            IOException e = new IOException();
            for (ErrnoException sup : suppressed) {
                addSuppressed(e, sup);
            }
            throw e;
        }
    }

    private void releaseChildWatches() {
        released.set(true);
        for (int wd : childDirs.keySet()) {
            try {
                inotify.removeWatch(fd, wd);
            } catch (ErrnoException e) {
                e.printStackTrace();
            }
        }
        childDirs.clear();
        notifyIncompleteObservationOrClose();
    }

    @Override
    public void run() {
        if (currentThread().isInterrupted() || closed.get()) {
            return;
        }

        setThreadPriority(THREAD_PRIORITY_BACKGROUND);

        try {
            thread.set(currentThread());
            observe(fd);
        } catch (Throwable e) {
            if (!isClosed()) {
                throw e;
            }
        }
        if (!isClosed()) {
            throw new IllegalStateException("Abnormal termination.");
        }
    }

    private native void observe(int fd);

    @SuppressWarnings("unused") // Called from native code
    private void onEvent(int wd, int event, byte[] child) {
        try {
            handleEvent(wd, event, child);
        } catch (final Exception e) {
            try {
                close();
            } catch (Throwable sup) {
                addSuppressed(e, sup);
            }
            throwOnMainThread(e);
        }
    }

    private static void throwOnMainThread(final Exception e) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void handleEvent(int wd, int event, byte[] child) throws IOException {
        // Disable to avoid getting called on large number of events,
        // even just calling isVerboseEnabled has some overhead,
        // enable when needed for debugging
        // log(wd, event, child);

        if (isClosed()) {
            return;
        }

        if (wd == this.wd) {

            if (isChildCreated(event, child)) {
                if (isDirectory(event)) {
                    addWatchForNewDirectory(child);
                }
                observer(CREATE, child);

            } else if (isChildDeleted(event, child)) {
                if (isDirectory(event)) {
                    removeChildWatch(child);
                }
                observer(DELETE, child);

            } else if (isSelfDeleted(event, child)) {
                close();
                observer(DELETE, (byte[]) null);

            } else if (isChildModified(event, child)) {
                observer(MODIFY, child);

            } else if (isSelfModified(event, child)) {
                observer(MODIFY, (byte[]) null);

            } else if (isObserverStopped(event)) {
                onObserverStopped(wd);

            } else {
                throw new RuntimeException(eventNames(event) + ": " + new String(child, UTF_8));
            }

        } else {
            if (isObserverStopped(event)) {
                onObserverStopped(wd);

            } else {
                LocalName childDirectory = childDirs.get(wd);
                if (childDirectory != null) {
                    observer(MODIFY, childDirectory);
                }
            }
        }
    }

    private void observer(Event kind, byte[] name) {
        notifyEventOrClose(kind, name == null ? null : LocalName.wrap(name));
    }

    private void observer(Event kind, LocalName name) {
        notifyEventOrClose(kind, name);
    }

    private void notifyEventOrClose(Event kind, LocalName name) {
        Observer observer = observerRef.get();
        if (observer != null) {
            observer.onEvent(kind, name);
        } else {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addWatchForNewDirectory(byte[] name) throws IOException {
        if (released.get()) {
            return;
        }

        try {

            byte[] path = root.resolve(name).path;
            int wd = inotify.addWatch(fd, path, CHILD_DIR_MASK);
            childDirs.put(wd, LocalName.wrap(name));

        } catch (ErrnoException e) {
            handleAddChildDirWatchFailure(e);
        }
    }

    private void handleAddChildDirWatchFailure(ErrnoException e) throws IOException {
        if (e.errno == ENOENT) {
            // Ignore

        } else if (e.errno == ENOSPC || e.errno == ENOMEM) {

            releaseChildWatches();

        } else if ( e.errno == EACCES) {

            notifyIncompleteObservationOrClose();

        } else {
            throw ErrnoExceptions.toIOException(e);
        }
    }

    private void notifyIncompleteObservationOrClose() {
        Observer observer = observerRef.get();
        if (observer != null) {
            observer.onIncompleteObservation();
        } else {
            try {
                close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void removeChildWatch(byte[] child) {
        Integer wd = childDirs.remove2(LocalName.wrap(child));
        if (wd != null) {
            try {
                inotify.removeWatch(fd, wd);
            } catch (ErrnoException ignored) {
            }
        }
    }

    private boolean isDirectory(int event) {
        return 0 != (event & IN_ISDIR);
    }

    private void onObserverStopped(int wd) {
        childDirs.remove(wd);
    }

    private boolean isObserverStopped(int event) {
        return 0 != (event & IN_IGNORED);
    }

    private boolean isChildCreated(int mask, byte[] child) {
        return (child != null && 0 != (mask & IN_CREATE)) ||
                (child != null && 0 != (mask & IN_MOVED_TO));
    }

    private boolean isChildModified(int mask, byte[] child) {
        return (child != null && 0 != (mask & IN_ATTRIB)) ||
                (child != null && 0 != (mask & IN_MODIFY)) ||
                (child != null && 0 != (mask & IN_CLOSE_WRITE));
    }

    private boolean isChildDeleted(int event, byte[] child) {
        return (child != null && 0 != (event & IN_MOVED_FROM)) ||
                (child != null && 0 != (event & IN_DELETE));
    }

    private boolean isSelfModified(int mask, byte[] child) {
        return (child == null && 0 != (mask & IN_ATTRIB)) ||
                (child == null && 0 != (mask & IN_MODIFY));
    }

    private boolean isSelfDeleted(int mask, byte[] child) {
        return (child == null && 0 != (mask & IN_DELETE_SELF)) ||
                (child == null && 0 != (mask & IN_MOVE_SELF));
    }

    private List<String> eventNames(int event) {
        List<String> events = new ArrayList<>(1);
        if (0 != (event & IN_OPEN)) events.add("IN_OPEN");
        if (0 != (event & IN_ACCESS)) events.add("IN_ACCESS");
        if (0 != (event & IN_ATTRIB)) events.add("IN_ATTRIB");
        if (0 != (event & IN_CREATE)) events.add("IN_CREATE");
        if (0 != (event & IN_DELETE)) events.add("IN_DELETE");
        if (0 != (event & IN_MODIFY)) events.add("IN_MODIFY");
        if (0 != (event & IN_MOVED_TO)) events.add("IN_MOVED_TO");
        if (0 != (event & IN_MOVE_SELF)) events.add("IN_MOVE_SELF");
        if (0 != (event & IN_MOVED_FROM)) events.add("IN_MOVED_FROM");
        if (0 != (event & IN_CLOSE_WRITE)) events.add("IN_CLOSE_WRITE");
        if (0 != (event & IN_DELETE_SELF)) events.add("IN_DELETE_SELF");
        if (0 != (event & IN_CLOSE_NOWRITE)) events.add("IN_CLOSE_NOWRITE");
        if (0 != (event & IN_IGNORED)) events.add("IN_IGNORED");
        if (0 != (event & IN_Q_OVERFLOW)) events.add("IN_Q_OVERFLOW");
        if (0 != (event & IN_UNMOUNT)) events.add("IN_UNMOUNT");
        if (0 != (event & IN_ISDIR)) events.add("IN_ISDIR");
        return events;
    }

//    private void log(int wd, int event, byte[] child) {
//        Path path = null;
//        if (wd == this.wd) {
//            path = root;
//        } else {
//            LocalName name = childDirs.get(wd);
//            if (name != null) {
//                path = root.resolve(name);
//            }
//        }
//
//        android.util.Log.v(getClass().getSimpleName(), "" +
//                "tag=" + tag +
//                ", fd=" + fd +
//                ", wd=" + wd +
//                ", event=" + eventNames(event) +
//                ", parent=" + path +
//                ", child=" + (child != null ? new LocalName(child) : null));
//    }

}
