package l.files.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import l.files.fs.event.BatchObserver;
import l.files.fs.event.BatchObserverNotifier;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

public abstract class FileSystem {

    public abstract void setPermissions(Path path, Set<Permission> permissions)
            throws IOException;

    public abstract void setLastModifiedTime(Path path, LinkOption option, Instant instant)
            throws IOException;

    public abstract Stat stat(Path path, LinkOption option)
            throws IOException;

    public abstract Path createDir(Path path)
            throws IOException;

    /**
     * Creates directory with specified permissions,
     * the set of permissions with be restricted so
     * the resulting permissions may not be the same.
     */
    public abstract Path createDir(Path path, Set<Permission> permissions)
            throws IOException;

    public abstract Path createFile(Path path)
            throws IOException;

    /**
     * @param link   the link itself
     * @param target the target the link will point to
     */
    public abstract Path createSymbolicLink(Path link, Path target)
            throws IOException;

    public abstract Path readSymbolicLink(Path path)
            throws IOException;

    /**
     * Moves src file tree to dst, destination must not exist.
     * <p/>
     * If src is a link, the link itself is moved, link target file is
     * unaffected.
     */
    public abstract void move(Path src, Path dst)
            throws IOException;

    public abstract void delete(Path path)
            throws IOException;

    public abstract boolean exists(Path path, LinkOption option)
            throws IOException;

    /**
     * Returns true if this file is readable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public abstract boolean isReadable(Path path)
            throws IOException;

    /**
     * Returns true if this file is writable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public abstract boolean isWritable(Path path)
            throws IOException;

    /**
     * Returns true if this file is executable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public abstract boolean isExecutable(Path path)
            throws IOException;

    /**
     * Observes on this file for change events.
     * <p/>
     * If this file is a directory, adding/removing immediate children and
     * any changes to the content/attributes of immediate children of this
     * directory will be notified, this is true for existing children as well as
     * newly added items after observation started.
     * <p/>
     * Note that by the time a listener is notified, the target file may
     * have already be changed again, therefore a robust application should have
     * an alternative way of handling instead of reply on this fully.
     * <p/>
     * The returned observation is closed if failed to observe.
     *
     * @param option           if option is {@link LinkOption#NOFOLLOW} and
     *                         this file is a link, observe on the link instead
     *                         of the link target
     * @param childrenConsumer consumer will be called for all immediate
     *                         children of {@code path}
     * @param logTag           tag for debug logging
     * @param watchLimit       limit the number of watch descriptors, or -1
     */
    public abstract Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer<? super Path> childrenConsumer,
            @Nullable String logTag,
            int watchLimit)
            throws IOException, InterruptedException;

    public Observation observe(
            Path path,
            LinkOption option,
            Observer observer)
            throws IOException, InterruptedException {

        return observe(path, option, observer, new Consumer<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                return true;
            }
        }, null, -1);
    }

    public Observation observe(
            Path path,
            LinkOption option,
            BatchObserver batchObserver,
            Consumer<? super Path> childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit,
            boolean quickNotifyFirstEvent,
            String tag,
            int watchLimit)
            throws IOException, InterruptedException {

        return new BatchObserverNotifier(
                batchObserver,
                batchInterval,
                batchInternalUnit,
                quickNotifyFirstEvent,
                tag,
                watchLimit
        ).start(this, path, option, childrenConsumer);
    }

    public abstract void list(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException;


    public <C extends Collection<? super Path>> C list(
            final Path path,
            final LinkOption option,
            final C collection) throws IOException {

        list(path, option, new Consumer<Path>() {
            @Override
            public boolean accept(Path entry) throws IOException {
                collection.add(entry);
                return true;
            }
        });
        return collection;
    }

    public void traverse(
            Path path,
            LinkOption option,
            TraversalCallback<? super Path> visitor) throws IOException {

        traverse(path, option, visitor, null);
    }

    /**
     * Performs a depth first traverse of this tree.
     * <p/>
     * e.g. traversing the follow tree:
     * <pre>
     *     a
     *    / \
     *   b   c
     * </pre>
     * will generate:
     * <pre>
     * visitor.onPreVisit(a)
     * visitor.onPreVisit(b)
     * visitor.onPostVisit(b)
     * visitor.onPreVisit(c)
     * visitor.onPostVisit(c)
     * visitor.onPostVisit(a)
     * </pre>
     *
     * @param option applies to root only, child links are never followed
     */
    public void traverse(
            Path path,
            LinkOption option,
            TraversalCallback<? super Path> visitor,
            @Nullable Comparator<Path> childrenComparator) throws IOException {

        new Traverser(path, this, option, visitor, childrenComparator)
                .traverse();
    }

    public abstract InputStream newInputStream(Path path)
            throws IOException;

    public abstract OutputStream newOutputStream(Path path, boolean append)
            throws IOException;

    public interface Consumer<E> {
        /**
         * @return true to continue, false to stop for multi-item callbacks
         */
        boolean accept(E entry) throws IOException;
    }
}
