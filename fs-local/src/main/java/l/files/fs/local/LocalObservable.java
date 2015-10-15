package l.files.fs.local;

import android.os.Handler;
import android.system.ErrnoException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import l.files.fs.Event;
import l.files.fs.File;
import l.files.fs.FileConsumer;
import l.files.fs.LinkOption;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Stream;

import static android.os.Looper.getMainLooper;
import static android.system.OsConstants.EINVAL;
import static android.system.OsConstants.ENOSPC;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static l.files.fs.Event.CREATE;
import static l.files.fs.Event.DELETE;
import static l.files.fs.Event.MODIFY;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.local.Dirent.DT_DIR;
import static l.files.fs.local.ErrnoExceptions.toIOException;
import static l.files.fs.local.Inotify.IN_ACCESS;
import static l.files.fs.local.Inotify.IN_ATTRIB;
import static l.files.fs.local.Inotify.IN_CLOSE_NOWRITE;
import static l.files.fs.local.Inotify.IN_CLOSE_WRITE;
import static l.files.fs.local.Inotify.IN_CREATE;
import static l.files.fs.local.Inotify.IN_DELETE;
import static l.files.fs.local.Inotify.IN_DELETE_SELF;
import static l.files.fs.local.Inotify.IN_DONT_FOLLOW;
import static l.files.fs.local.Inotify.IN_EXCL_UNLINK;
import static l.files.fs.local.Inotify.IN_IGNORED;
import static l.files.fs.local.Inotify.IN_ISDIR;
import static l.files.fs.local.Inotify.IN_MODIFY;
import static l.files.fs.local.Inotify.IN_MOVED_FROM;
import static l.files.fs.local.Inotify.IN_MOVED_TO;
import static l.files.fs.local.Inotify.IN_MOVE_SELF;
import static l.files.fs.local.Inotify.IN_ONLYDIR;
import static l.files.fs.local.Inotify.IN_OPEN;
import static l.files.fs.local.Inotify.IN_Q_OVERFLOW;
import static l.files.fs.local.Inotify.IN_UNMOUNT;
import static l.files.fs.local.Inotify.addWatch;

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

    private static final Handler handler = new Handler(getMainLooper());

    /**
     * Mask to use for root file.
     */
    private static final int ROOT_MASK
            = IN_EXCL_UNLINK
            | IN_ATTRIB
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
    private static final int CHILD_DIRECTORY_MASK
            = IN_DONT_FOLLOW
            | IN_EXCL_UNLINK
            | IN_ONLYDIR
            | IN_CREATE
            | IN_DELETE
            | IN_MOVED_FROM
            | IN_MOVED_TO;

    static {
        init();
    }

    private static native void init();

    /**
     * The file descriptor of the inotify instance.
     */
    private final int fd;

    /**
     * The watch descriptor of {@link #root}.
     */
    private final int wd;

    /**
     * The root file being watched.
     */
    private final File root;

    /**
     * If {@link #root} is a directory, its immediate child directories will
     * also be watched since creation of files inside a child directory will
     * update the child directory's attributes, and that update won't be
     * reported by other events. This is a map of watch descriptors to the child
     * directories name being watched, and it will be updated as directories are
     * being created/deleted.
     */
    private final Map<Integer, String> childDirectories;

    // TODO use weak reference
    private final Observer observer;

    /**
     * Background thread that polls for events.
     */
    private final AtomicReference<Thread> thread;

    private final AtomicBoolean closed;

    LocalObservable(int fd, int wd, LocalFile root, Observer observer) {
        this.fd = fd;
        this.wd = wd;
        this.root = requireNonNull(root, "root");
        this.observer = requireNonNull(observer, "observer");
        this.childDirectories = new ConcurrentHashMap<>();
        this.thread = new AtomicReference<>(null);
        this.closed = new AtomicBoolean(false);
    }

    public static Observation observe(
            LocalFile file,
            LinkOption option,
            Observer observer,
            FileConsumer childrenConsumer) throws IOException, InterruptedException {

        requireNonNull(file, "root");
        requireNonNull(option, "option");
        requireNonNull(observer, "observer");

        boolean directory = file.stat(option).isDirectory();
        boolean observeSuccess = true;

        int fd;
        int wd;
        try {
            fd = Inotify.init();
            wd = inotifyAddWatchWillCloseOnError(fd, file, option);
        } catch (ErrnoException e) {
            fd = -1;
            wd = -1;
            observeSuccess = false;
        }

        LocalObservable observable = new LocalObservable(fd, wd, file, observer);

        /* Using a new thread seems to be much quicker than using a thread pool
         * executor, didn't investigate why, just a reminder here to check the
         * performance if this is to be changed to a pool.
         */
        Thread thread = new Thread(observable);
        thread.setName(observable.toString());
        try {

            /*
             * Start the thread then observe on the children directories, this
             * allows them to happen at the same time, just need to make sure the
             * latest one wins.
             */
            if (observeSuccess) {
                thread.start();
            }

            if (directory
                    && observeSuccess
                    && !observeChildren(fd, file, option, observable, childrenConsumer)) {
                observeSuccess = false;
            }

        } catch (Throwable e) {
            thread.interrupt();
            try {
                observable.close();
            } catch (Exception sup) {
                e.addSuppressed(sup);
            }
            throw e;
        }

        if (!observeSuccess) {
            thread.interrupt();
            observable.close();
        }

        return observable;
    }

    private static int inotifyAddWatchWillCloseOnError(int fd, File file, LinkOption opt)
            throws ErrnoException {

        try {

            int mask = ROOT_MASK | (opt == NOFOLLOW ? IN_DONT_FOLLOW : 0);
            return addWatch(fd, file.path(), mask);

        } catch (Throwable e) {
            try {
                Unistd.close(fd);
            } catch (ErrnoException ee) {
                e.addSuppressed(ee);
            }
            throw e;
        }
    }

    private static boolean observeChildren(
            int fd,
            LocalFile file,
            LinkOption option,
            LocalObservable observable,
            FileConsumer childrenConsumer) throws IOException, InterruptedException {

        boolean observe = fd != -1;
        try (Stream<Dirent> stream = Dirent.stream(file, option, false)) {
            for (Dirent child : stream) {

                File childFile = file.resolve(child.name());
                childrenConsumer.accept(childFile);

                if (currentThread().isInterrupted()) {
                    throw new InterruptedException();
                }

                if (observe && child.type() == DT_DIR) {
                    try {

                        String path = file.path() + "/" + child.name();
                        int wd = addWatch(fd, path, CHILD_DIRECTORY_MASK);
                        observable.childDirectories.put(wd, child.name());

                    } catch (ErrnoException e) {
                        // TODO handle other types
                        if (e.errno == ENOSPC) {
                            observe = false;
                        }
                    }
                }

            }
        }

        return observe;
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
        for (Integer wd : childDirectories.keySet()) {
            try {
                Inotify.removeWatch(fd, wd);
            } catch (ErrnoException e) {
                if (e.errno != EINVAL) {
                    suppressed.add(e);
                }
            }
        }

        if (wd != -1) {
            try {
                Inotify.removeWatch(fd, wd);
            } catch (ErrnoException e) {
                if (e.errno != EINVAL) {
                    suppressed.add(e);
                }
            }
        }

        try {

            Unistd.close(fd);

        } catch (ErrnoException e) {
            for (ErrnoException sup : suppressed) {
                e.addSuppressed(sup);
            }
            throw toIOException(e, root.path());
        }

        if (!suppressed.isEmpty()) {
            IOException e = new IOException();
            for (ErrnoException sup : suppressed) {
                e.addSuppressed(sup);
            }
            throw e;
        }
    }

    @Override
    public void run() {
        if (currentThread().isInterrupted() || closed.get()) {
            return;
        }
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

    // Also called from native code
    @SuppressWarnings("UnusedDeclaration")
    private void onEvent(int wd, int event, String child) {
        try {
            handleEvent(wd, event, child);
        } catch (final Exception e) {
            try {
                close();
            } catch (Throwable ee) {
                e.addSuppressed(ee);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    throw e;
                }
            });
        }
    }

    private void handleEvent(int wd, int event, String child) {
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
                removeWatch(wd);
                observer(DELETE, child);

            } else if (isSelfDeleted(event, child)) {
                removeWatch(wd);
                observer(DELETE, null);

            } else if (isChildModified(event, child)) {
                observer(MODIFY, child);

            } else if (isSelfModified(event, child)) {
                observer(MODIFY, null);

            } else if (isObserverStopped(event)) {
                onObserverStopped(wd);

            } else {
                throw new RuntimeException(eventName(event) + ": " + child);
            }

        } else {
            if (isObserverStopped(event)) {
                onObserverStopped(wd);
            } else {
                String childDirectory = childDirectories.get(wd);
                if (childDirectory != null) {
                    observer(MODIFY, childDirectory);
                }
            }
        }
    }

    private void observer(Event kind, String name) {
        observer.onEvent(kind, name);
    }

    private void addWatchForNewDirectory(String name) {
        try {
            String path = root.path() + "/" + name;
            int wd = addWatch(fd, path, CHILD_DIRECTORY_MASK);
            childDirectories.put(wd, name);
        } catch (ErrnoException e) {
//            TODO handle other types
            if (e.errno == ENOSPC) {
                try {
                    close();
                } catch (IOException ignored) {
                }
                observer.onCancel();
            }
        }
    }

    private void removeWatch(int wd) {
        childDirectories.remove(wd);
    }

    private boolean isDirectory(int event) {
        return 0 != (event & IN_ISDIR);
    }

    private void onObserverStopped(int wd) {
        childDirectories.remove(wd);
    }

    private boolean isObserverStopped(int event) {
        return 0 != (event & IN_IGNORED);
    }

    private boolean isChildCreated(int mask, String child) {
        return (child != null && 0 != (mask & IN_CREATE)) ||
                (child != null && 0 != (mask & IN_MOVED_TO));
    }

    private boolean isChildModified(int mask, String child) {
        return (child != null && 0 != (mask & IN_ATTRIB)) ||
                (child != null && 0 != (mask & IN_MODIFY)) ||
                (child != null && 0 != (mask & IN_CLOSE_WRITE));
    }

    private boolean isChildDeleted(int event, String child) {
        return (child != null && 0 != (event & IN_MOVED_FROM)) ||
                (child != null && 0 != (event & IN_DELETE));
    }

    private boolean isSelfModified(int mask, String child) {
        return (child == null && 0 != (mask & IN_ATTRIB)) ||
                (child == null && 0 != (mask & IN_MODIFY));
    }

    private boolean isSelfDeleted(int mask, String child) {
        return (child == null && 0 != (mask & IN_DELETE_SELF)) ||
                (child == null && 0 != (mask & IN_MOVE_SELF));
    }

    private String eventName(int event) {
        if (0 != (event & IN_OPEN)) return "IN_OPEN";
        if (0 != (event & IN_ACCESS)) return "IN_ACCESS";
        if (0 != (event & IN_ATTRIB)) return "IN_ATTRIB";
        if (0 != (event & IN_CREATE)) return "IN_CREATE";
        if (0 != (event & IN_DELETE)) return "IN_DELETE";
        if (0 != (event & IN_MODIFY)) return "IN_MODIFY";
        if (0 != (event & IN_MOVED_TO)) return "IN_MOVED_TO";
        if (0 != (event & IN_MOVE_SELF)) return "IN_MOVE_SELF";
        if (0 != (event & IN_MOVED_FROM)) return "IN_MOVED_FROM";
        if (0 != (event & IN_CLOSE_WRITE)) return "IN_CLOSE_WRITE";
        if (0 != (event & IN_DELETE_SELF)) return "IN_DELETE_SELF";
        if (0 != (event & IN_CLOSE_NOWRITE)) return "IN_CLOSE_NOWRITE";
        if (0 != (event & IN_IGNORED)) return "IN_IGNORED";
        if (0 != (event & IN_Q_OVERFLOW)) return "IN_Q_OVERFLOW";
        if (0 != (event & IN_UNMOUNT)) return "IN_UNMOUNT";
        return "UNKNOWN";
    }

//    private void log(int wd, int event, String child) {
//        File file;
//        if (wd == this.wd) {
//            file = root;
//        } else {
//            file = root.resolve(childDirectories.get(wd));
//        }
//
//        Log.v(getClass().getSimpleName(), "fd=" + fd +
//                ", wd=" + wd +
//                ", event=" + eventName(event) +
//                ", parent=" + file +
//                ", child=" + child);
//    }

}
