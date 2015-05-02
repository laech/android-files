package l.files.fs.local;

import android.os.Handler;

import com.google.common.base.Throwables;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import l.files.fs.LinkOption;
import l.files.fs.Resource;
import l.files.fs.UncheckedIOException;
import l.files.fs.WatchEvent;
import l.files.logging.Logger;

import static android.os.Looper.getMainLooper;
import static java.lang.Thread.UncaughtExceptionHandler;
import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.WatchEvent.Kind;
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
import static l.files.fs.local.Inotify.IN_NONBLOCK;
import static l.files.fs.local.Inotify.IN_ONLYDIR;
import static l.files.fs.local.Inotify.IN_OPEN;
import static l.files.fs.local.Inotify.IN_Q_OVERFLOW;
import static l.files.fs.local.Inotify.IN_UNMOUNT;

final class LocalResourceObservable extends Native
        implements Runnable, Closeable {

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

    private static final Logger log = Logger.get(LocalResourceObservable.class);

    private static final Handler handler = new Handler(getMainLooper());

    /**
     * Mask to use for root resource.
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
     * The watch descriptor of {@link #resource}.
     */
    private final int wd;

    /**
     * The root resource being watched.
     */
    private final LocalResource resource;

    /**
     * If {@link #resource} is a directory, its immediate child directories will
     * also be watched since creation of resources inside a child directory will
     * update the child directory's attributes, and that update won't be
     * reported by other events. This is a map of watch descriptors to the child
     * directories being watched, and it will be updated as directories are
     * being created/deleted.
     */
    private final Map<Integer, Resource> children;

    // TODO use weak reference
    private final WatchEvent.Listener observer;

    /**
     * Background thread that polls for events.
     */
    private final AtomicReference<Thread> thread;

    private final AtomicBoolean closed;

    LocalResourceObservable(int fd,
                            int wd,
                            LocalResource resource,
                            WatchEvent.Listener observer) {
        this.fd = fd;
        this.wd = wd;
        this.resource = requireNonNull(resource, "resource");
        this.observer = requireNonNull(observer, "observer");
        this.children = new ConcurrentHashMap<>();
        this.thread = new AtomicReference<>(null);
        this.closed = new AtomicBoolean(false);
    }

    public static Closeable observe(
            LocalResource resource,
            LinkOption option,
            WatchEvent.Listener observer) throws IOException {

        requireNonNull(resource, "resource");
        requireNonNull(option, "option");
        requireNonNull(observer, "observer");

        boolean directory = resource.stat(option).isDirectory();

        int fd = inotifyInit(resource);
        int wd = inotifyAddWatchWillCloseOnError(fd, resource, option);

        LocalResourceObservable observable =
                new LocalResourceObservable(fd, wd, resource, observer);

        /* Using a new thread seems to be much quicker than using a thread pool
         * executor, didn't investigate why, just a reminder here to check the
         * performance if this is to be changed to a pool.
         */
        Thread thread = new Thread(observable);
        thread.setName(observable.toString());
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, final Throwable e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        throw Throwables.propagate(e);
                    }
                });
            }
        });

        /*
         * Start the thread then observe on the children directories, this
         * allows them to happen at the same time, just need to make sure the
         * latest one wins.
         */
        thread.start();

        if (!directory) {
            return observable;
        }

        try {
            observeChildren(fd, resource, option, observable);
        } catch (UncheckedIOException | IOException e) {
            log.warn(e);
        }

        return observable;
    }

    private static int inotifyInit(LocalResource resource) throws IOException {
        try {
            return Inotify.init1(IN_NONBLOCK);
        } catch (ErrnoException e) {
            throw e.toIOException(resource.path());
        }
    }

    private static int inotifyAddWatchWillCloseOnError(
            int fd,
            LocalResource resource,
            LinkOption option) throws IOException {

        try {

            int mask = ROOT_MASK;
            if (option == NOFOLLOW) {
                mask |= IN_DONT_FOLLOW;
            }
            String path = resource.getFile().getPath();
            return Inotify.addWatch(fd, path, mask);

        } catch (ErrnoException e) {
            try {
                Unistd.close(fd);
            } catch (ErrnoException ee) {
                e.addSuppressed(ee);
            }
            throw e.toIOException(resource.path());

        } catch (Throwable e) {
            try {
                Unistd.close(fd);
            } catch (ErrnoException ee) {
                e.addSuppressed(ee);
            }
            throw e;
        }
    }

    private static void observeChildren(
            final int fd,
            final LocalResource resource,
            final LinkOption option,
            final LocalResourceObservable observable) throws IOException {

        LocalResourceStream.list(resource, option, new LocalResourceStream.Callback() {
            @Override
            public boolean accept(long inode, String name, boolean directory) throws IOException {
                if (observable.isClosed()) {
                    return false;
                }
                if (!directory) {
                    return true;
                }

                LocalResource child = resource.resolve(name);
                try {

                    String path = child.getFile().getPath();
                    int wd = Inotify.addWatch(fd, path, CHILD_DIRECTORY_MASK);
                    observable.children.put(wd, child);

                } catch (ErrnoException e) {
                    log.debug("Failed to add watch. %s: %s", e.getMessage(), child);
                }

                return true;
            }
        });

    }

    @Override
    public String toString() {
        return getClass().getSimpleName()
                + "{fd=" + fd + ",wd=" + wd + ",resource=" + resource + "}";
    }

    // Also called from native code
    private boolean isClosed() {
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

        try {
            Unistd.close(fd);
        } catch (ErrnoException e) {
            throw e.toIOException(resource.path());
        }
    }

    @Override
    public void run() {
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
    private void sleep() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            // Interrupt from close()
        }
    }

    // Also called from native code
    @SuppressWarnings("UnusedDeclaration")
    private void onEvent(int wd, int event, String child) {
        try {
            handleEvent(wd, event, child);
        } catch (final Throwable e) {
            try {
                close();
            } catch (Throwable ee) {
                e.addSuppressed(ee);
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    throw Throwables.propagate(e);
                }
            });
        }
    }

    private void handleEvent(int wd, int event, String child) {
        log(wd, event, child);

        if (isClosed()) {
            return;
        }

        if (wd == this.wd) {

            if (isChildCreated(event, child)) {
                LocalResource childResource = this.resource.resolve(child);
                if (isDirectory(event)) {
                    addWatchForDirectory(childResource);
                }
                observer(childResource, Kind.CREATE);

            } else if (isChildDeleted(event, child)) {
                removeWatch(wd);
                observer(resource.resolve(child), Kind.DELETE);

            } else if (isSelfDeleted(event, child)) {
                removeWatch(wd);
                observer(resource, Kind.DELETE);

            } else if (isChildModified(event, child)) {
                observer(resource.resolve(child), Kind.MODIFY);

            } else if (isSelfModified(event, child)) {
                observer(resource, Kind.MODIFY);

            } else if (isObserverStopped(event)) {
                onObserverStopped(wd);

            } else {
                throw new RuntimeException(getEventName(event) + ": " + child);
            }

        } else {

            if (isObserverStopped(event)) {
                onObserverStopped(wd);

            } else {
                Resource resource = children.get(wd);
                if (resource != null) {
                    observer(resource, Kind.MODIFY);
                }
            }

        }
    }

    private void observer(Resource resource, Kind kind) {
        observer.onEvent(WatchEvent.create(kind, resource));
    }

    private void addWatchForDirectory(LocalResource directory) {
        try {
            String path = directory.getFile().getPath();
            int wd = Inotify.addWatch(fd, path, CHILD_DIRECTORY_MASK);
            children.put(wd, directory);
        } catch (ErrnoException e) {
            log.warn(e, "Failed to add watch %s", directory);
        }
    }

    private void removeWatch(int wd) {
        children.remove(wd);
    }

    private boolean isDirectory(int event) {
        return 0 != (event & IN_ISDIR);
    }

    private void onObserverStopped(int wd) {
        children.remove(wd);
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

    private String getEventName(int event) {
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

    private void log(int wd, int event, String child) {
        Resource resource;
        if (wd == this.wd) {
            resource = this.resource;
        } else {
            resource = children.get(wd);
        }
        log.verbose("fd=%s, wd=%s, event=%s, parent=%s, child=%s",
                fd, wd, getEventName(event), resource, child);
    }

}
