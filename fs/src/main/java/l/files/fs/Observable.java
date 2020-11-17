package l.files.fs;

import android.os.Handler;
import android.util.Log;
import androidx.annotation.Nullable;
import l.files.fs.event.Event;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;
import linux.Dirent;
import linux.Dirent.DIR;
import linux.ErrnoException;
import linux.Vfs.Statfs;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static android.os.Looper.getMainLooper;
import static android.os.Process.THREAD_PRIORITY_BACKGROUND;
import static android.os.Process.setThreadPriority;
import static java.lang.Thread.currentThread;
import static l.files.base.Objects.requireNonNull;
import static l.files.base.Throwables.addSuppressed;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.event.Event.*;
import static linux.Errno.*;
import static linux.Inotify.*;
import static linux.Vfs.PROC_SUPER_MAGIC;
import static linux.Vfs.statfs;

final class Observable extends Native
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
     *  - https://code.google.com/p/android-developer-preview/issues/detail
     * ?id=3099
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

    private volatile OptionalInt fd = OptionalInt.empty();
    private volatile OptionalInt wd = OptionalInt.empty();

    private final Path root;

    @Nullable
    private final String tag;

    /**
     * If {@link #root} is a directory, its immediate child directories will
     * also be watched since creation of files inside a child directory will
     * update the child directory's attributes, and that update won't be
     * reported by other events. This is a map of watch descriptors to the child
     * directories name being watched, and it will be updated as directories are
     * being created/deleted.
     */
    private final ConcurrentBiMap<Integer, Path> childDirFileNames =
        new ConcurrentBiMap<>();

    private final WeakReference<Observer> observerRef;

    /**
     * Background thread that polls for events.
     */
    private final AtomicReference<Thread> thread;
    private final AtomicReference<Throwable> closeReason;
    private final AtomicBoolean closed;
    private final AtomicBoolean released;

    Observable(Path root, Observer observer) {
        this(root, observer, null);
    }

    Observable(Path root, Observer observer, @Nullable String tag) {
        this.root = requireNonNull(root);
        this.observerRef = new WeakReference<>(requireNonNull(observer));
        this.thread = new AtomicReference<>(null);
        this.closed = new AtomicBoolean(false);
        this.closeReason = new AtomicReference<>(null);
        this.released = new AtomicBoolean(false);
        this.tag = tag;
    }

    private static int getOrThrow(OptionalInt optional) {
        if (!optional.isPresent()) {
            throw new IllegalStateException();
        }
        return optional.getAsInt();
    }

    void start(
        LinkOption option,
        java.util.function.Consumer<java.nio.file.Path> childrenConsumer,
        int watchLimit
    )
        throws IOException, InterruptedException {

        requireNonNull(option);
        requireNonNull(childrenConsumer);

        try {
            if (!isProcfs(root.toByteArray())) {
                int fd = inotify.init(watchLimit);
                int wd = inotifyAddWatchWillCloseOnError(fd, option);
                this.fd = OptionalInt.of(fd);
                this.wd = OptionalInt.of(wd);
            } else {
                doClose(new IOException("procfs not supported"));
            }
        } catch (ErrnoException e) {
            fd = OptionalInt.empty();
            wd = OptionalInt.empty();
            suppressedClose(e);
            notifyIncompleteObservationOrClose(e, root);
            return;
        }

        /* Using a new thread seems to be much quicker than using a thread pool
         * executor, didn't investigate why, just a reminder here to check the
         * performance if this is to be changed to a pool.
         */
        Thread thread = null;
        try {

            /*
             * Start the thread then observe on the children directories,
             * this allows them to happen at the same time, just need to make
             * sure the latest one wins.
             */
            if (fd.isPresent()) {
                thread = new Thread(this);
                thread.setName(toString());
                thread.start();
            }

            if (root.stat(option).isDirectory()) {
                traverseChildren(childrenConsumer);
            }

        } catch (Throwable e) {

            if (thread != null) {
                thread.interrupt();
            }
            suppressedClose(e);
            throw e;
        }

    }

    private int inotifyAddWatchWillCloseOnError(int fd, LinkOption opt)
        throws ErrnoException {

        try {

            int mask = ROOT_MASK | (opt == NOFOLLOW ? IN_DONT_FOLLOW : 0);
            return inotify.addWatch(fd, root.toByteArray(), mask);

        } catch (Throwable e) {
            try {
                doClose(e);
            } catch (ErrnoException sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }
    }

    private void traverseChildren(
        Consumer<java.nio.file.Path> childrenConsumer
    ) throws IOException, InterruptedException {

        OptionalInt fd = this.fd;
        boolean limitReached = !fd.isPresent();

        try {
            DIR dir = Dirent.opendir(root.toByteArray());
            try {
                Dirent entry = new Dirent();
                while ((entry = Dirent.readdir(dir, entry)) != null) {

                    if (entry.isSelfOrParent()) {
                        continue;
                    }

                    byte[] name = Arrays.copyOf(entry.d_name, entry.d_name_len);
                    Path child = root.concat(name);
                    childrenConsumer.accept(child.toJavaPath());

                    if (limitReached || entry.d_type != Dirent.DT_DIR ||
                        released.get()) {
                        continue;
                    }

                    try {

                        byte[] childPath = child.toByteArray();
                        int wd = inotify.addWatch(
                            fd.getAsInt(),
                            childPath,
                            CHILD_DIR_MASK
                        );
                        childDirFileNames.put(wd, Path.of(name));

                    } catch (ErrnoException e) {
                        handleAddChildDirWatchFailure(e, child);
                    }

                    if (currentThread().isInterrupted()) {
                        break;
                    }
                }
            } finally {
                Dirent.closedir(dir);
            }
        } catch (ErrnoException e) {
            throw e.toIOException(root);
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

    @Nullable
    @Override
    public Throwable closeReason() {
        return closeReason.get();
    }

    @Override
    public void close() throws IOException {
        try {
            doClose(new IOException("close() called"));
        } catch (ErrnoException e) {
            throw e.toIOException(root);
        }
    }

    private void suppressedClose(Throwable e) {
        try {
            doClose(e);
        } catch (Throwable sup) {
            addSuppressed(e, sup);
        }
    }

    private void doClose(Throwable cause) throws ErrnoException {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        closeReason.set(cause);

        Thread t = thread.get();
        if (t != null) {
            t.interrupt();
        }

        List<ErrnoException> suppressed = new ArrayList<>(0);
        fd.ifPresent(fd -> {
            for (Integer wd : childDirFileNames.keySet()) {
                try {
                    inotify.removeWatch(fd, wd);
                } catch (ErrnoException e) {
                    if (e.errno != EINVAL) {
                        suppressed.add(e);
                    }
                }
            }

            wd.ifPresent(wd -> {
                try {
                    inotify.removeWatch(fd, wd);
                } catch (ErrnoException e) {
                    if (e.errno != EINVAL) {
                        suppressed.add(e);
                    }
                }
            });
        });

        try {

            inotify.close(fd, cause);

        } catch (ErrnoException e) {
            for (ErrnoException sup : suppressed) {
                addSuppressed(e, sup);
            }
            throw e;
        }

        if (!suppressed.isEmpty()) {
            ErrnoException e = suppressed.get(0);
            for (int i = 1; i < suppressed.size(); i++) {
                addSuppressed(e, suppressed.get(i));
            }
            throw e;
        }
    }

    private void releaseChildWatches(ErrnoException cause, Path path) {
        released.set(true);
        for (int wd : childDirFileNames.keySet()) {
            try {
                inotify.removeWatch(getOrThrow(fd), wd);
            } catch (ErrnoException e) {
                Log.w(getClass().getSimpleName(),
                    "Failed to remove watch on release" +
                        ", fd=" + fd +
                        ", wd=" + wd, e
                );
            }
        }
        childDirFileNames.clear();
        notifyIncompleteObservationOrClose(cause, path);
    }

    @Override
    public void run() {
        if (currentThread().isInterrupted() || closed.get()) {
            return;
        }

        setThreadPriority(THREAD_PRIORITY_BACKGROUND);

        try {
            thread.set(currentThread());
            observe(getOrThrow(this.fd));
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
    private void onEvent(int wd, int event, @Nullable byte[] child) {
        try {
            handleEvent(wd, event, child);
        } catch (Throwable e) {
            if (!isClosed() || !(e instanceof ErrnoException)) {
                suppressedClose(e);
                throwOnMainThread(e);
            }
        }
    }

    private static void throwOnMainThread(Throwable e) {
        handler.post(() -> {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleEvent(int wd, int event, @Nullable byte[] child)
        throws ErrnoException {
        // Disable to avoid getting called on large number of events,
        // even just calling isVerboseEnabled has some overhead,
        // enable when needed for debugging
        // log(wd, event, child);

        if (isClosed()) {
            return;
        }

        if (wd == getOrThrow(this.wd)) {

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
                doClose(new IOException("file is deleted"));
                observer(DELETE, (byte[]) null);

            } else if (isChildModified(event, child)) {
                observer(MODIFY, child);

            } else if (isSelfModified(event, child)) {
                observer(MODIFY, (byte[]) null);

            } else if (isObserverStopped(event)) {
                onObserverStopped(wd);

            } else {
                // FIXME
            }

        } else {
            if (isObserverStopped(event)) {
                onObserverStopped(wd);

            } else {
                Path childDirFileName = childDirFileNames.get(wd);
                if (childDirFileName != null) {
                    observer(MODIFY, childDirFileName);
                }
            }
        }
    }

    private void observer(Event kind, @Nullable byte[] name) {
        notifyEventOrClose(kind, name == null ? null : Path.of(name));
    }

    private void observer(Event kind, Path fileName) {
        notifyEventOrClose(kind, fileName);
    }

    private void notifyEventOrClose(Event kind, @Nullable Path childFileName) {
        Observer observer = observerRef.get();
        if (observer != null) {
            observer.onEvent(
                kind,
                childFileName != null ? childFileName.toJavaPath() : null
            );
        } else {
            try {
                doClose(new IOException("observer is gone"));
            } catch (ErrnoException e) {
                Log.w(getClass().getSimpleName(),
                    "Failed to close on notify event, "
                        + kind + ", " + childFileName, e
                );
            }
        }
    }

    private void addWatchForNewDirectory(byte[] name) throws ErrnoException {
        if (released.get() || closed.get()) {
            return;
        }

        Path child = root.concat(name);
        try {

            byte[] path = child.toByteArray();
            int wd =
                inotify.addWatch(getOrThrow(this.fd), path, CHILD_DIR_MASK);
            childDirFileNames.put(wd, Path.of(name));

        } catch (ErrnoException e) {
            handleAddChildDirWatchFailure(e, child);
        }
    }

    private void handleAddChildDirWatchFailure(
        ErrnoException e,
        Path path
    ) throws ErrnoException {

        if (e.errno == ENOENT) {
            // Ignore

        } else if (e.errno == ENOSPC || e.errno == ENOMEM) {

            releaseChildWatches(e, path);

        } else if (e.errno == EACCES) {

            notifyIncompleteObservationOrClose(e, path);

        } else {
            throw e;
        }
    }

    private void notifyIncompleteObservationOrClose(
        ErrnoException cause,
        Path path
    ) {
        Observer observer = observerRef.get();
        if (observer != null) {
            observer.onIncompleteObservation(cause.toIOException(path));
        } else {
            try {
                doClose(cause);
            } catch (ErrnoException e) {
                Log.w(getClass().getSimpleName(),
                    "Failed to close on incomplete observation.", e
                );
            }
        }
    }

    private void removeChildWatch(byte[] child) {
        Integer wd = childDirFileNames.remove2(Path.of(child));
        if (wd != null) {
            try {
                inotify.removeWatch(getOrThrow(fd), wd);
            } catch (ErrnoException ignored) {
            }
        }
    }

    private boolean isDirectory(int event) {
        return 0 != (event & IN_ISDIR);
    }

    private void onObserverStopped(int wd) {
        childDirFileNames.remove(wd);
    }

    private boolean isObserverStopped(int event) {
        return 0 != (event & IN_IGNORED);
    }

    private boolean isChildCreated(int mask, @Nullable byte[] child) {
        return (child != null && 0 != (mask & IN_CREATE))
            || (child != null && 0 != (mask & IN_MOVED_TO));
    }

    private boolean isChildModified(int mask, @Nullable byte[] child) {
        return (child != null && 0 != (mask & IN_ATTRIB))
            || (child != null && 0 != (mask & IN_MODIFY))
            || (child != null && 0 != (mask & IN_CLOSE_WRITE));
    }

    private boolean isChildDeleted(int event, @Nullable byte[] child) {
        return (child != null && 0 != (event & IN_MOVED_FROM))
            || (child != null && 0 != (event & IN_DELETE));
    }

    private boolean isSelfModified(int mask, @Nullable byte[] child) {
        return (child == null && 0 != (mask & IN_ATTRIB))
            || (child == null && 0 != (mask & IN_MODIFY));
    }

    private boolean isSelfDeleted(int mask, @Nullable byte[] child) {
        return (child == null && 0 != (mask & IN_DELETE_SELF))
            || (child == null && 0 != (mask & IN_MOVE_SELF));
    }

    private List<String> eventNames(int event) {
        List<String> events = new ArrayList<>(1);
        if (0 != (event & IN_OPEN)) {
            events.add("IN_OPEN");
        }
        if (0 != (event & IN_ACCESS)) {
            events.add("IN_ACCESS");
        }
        if (0 != (event & IN_ATTRIB)) {
            events.add("IN_ATTRIB");
        }
        if (0 != (event & IN_CREATE)) {
            events.add("IN_CREATE");
        }
        if (0 != (event & IN_DELETE)) {
            events.add("IN_DELETE");
        }
        if (0 != (event & IN_MODIFY)) {
            events.add("IN_MODIFY");
        }
        if (0 != (event & IN_MOVED_TO)) {
            events.add("IN_MOVED_TO");
        }
        if (0 != (event & IN_MOVE_SELF)) {
            events.add("IN_MOVE_SELF");
        }
        if (0 != (event & IN_MOVED_FROM)) {
            events.add("IN_MOVED_FROM");
        }
        if (0 != (event & IN_CLOSE_WRITE)) {
            events.add("IN_CLOSE_WRITE");
        }
        if (0 != (event & IN_DELETE_SELF)) {
            events.add("IN_DELETE_SELF");
        }
        if (0 != (event & IN_CLOSE_NOWRITE)) {
            events.add("IN_CLOSE_NOWRITE");
        }
        if (0 != (event & IN_IGNORED)) {
            events.add("IN_IGNORED");
        }
        if (0 != (event & IN_Q_OVERFLOW)) {
            events.add("IN_Q_OVERFLOW");
        }
        if (0 != (event & IN_UNMOUNT)) {
            events.add("IN_UNMOUNT");
        }
        if (0 != (event & IN_ISDIR)) {
            events.add("IN_ISDIR");
        }
        if (events.isEmpty()) {
            events.add("UNKNOWN:" + event);
        }
        return events;
    }

    //    private void log(int wd, int event, byte[] child) {
    //        Path path = null;
    //        if (wd == this.wd) {
    //            path = root;
    //        } else {
    //            LocalName name = childDirs.get(wd);
    //            if (name != null) {
    //                path = root.concat(name);
    //            }
    //        }
    //
    //        android.util.Log.v(getClass().getSimpleName(), "" +
    //                "tag=" + tag +
    //                ", fd=" + fd +
    //                ", wd=" + wd +
    //                ", event=" + eventNames(event) +
    //                ", parent=" + path +
    //                ", child=" + (child != null ? new LocalName(child) :
    //                null));
    //    }

}
