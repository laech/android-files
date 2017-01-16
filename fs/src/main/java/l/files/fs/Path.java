package l.files.fs;

import android.os.Parcelable;

import com.google.common.collect.ImmutableList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import l.files.fs.event.BatchObserver;
import l.files.fs.event.BatchObserverNotifier;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;

import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class Path implements Parcelable {

    public abstract byte[] toByteArray();

    /**
     * Returns a string representation of this path.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    @Override
    public abstract String toString();

    /**
     * Converts this path to a URI,
     * this method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    public abstract URI toUri();

    /**
     * If this is a relative path, converts it to an absolute path by
     * concatenating the current working directory with this path.
     * If this path is already an absolute path returns this.
     */
    public abstract Path toAbsolutePath();

    /**
     * Gets all the file names of this path. For example:
     * <pre>
     *     "/a/b/c" -> ["a", "b", "c"]
     * </pre>
     */
    public abstract ImmutableList<Name> names();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object o);

    /**
     * Concatenates {@code path} onto the end of this path.
     */
    public abstract Path concat(Path path);

    public abstract Path concat(Name name);

    public abstract Path concat(String path);

    public abstract Path concat(byte[] path);

    /**
     * Returns the parent file, if any. For example:
     * <pre>
     *     "/a/b" ->  "/a"
     *     "/a"   ->  "/"   (root path, absolute)
     *     "/"    ->  null
     *     "a"    ->  ""    (current working directory, relative)
     *     ""     ->  null
     * </pre>
     */
    @Nullable
    public abstract Path parent();

    public ImmutableList<Path> hierarchy() {
        ImmutableList.Builder<Path> hierarchy = ImmutableList.builder();
        for (Path p = this; p != null; p = p.parent()) {
            hierarchy.add(p);
        }
        return hierarchy.build().reverse();
    }

    /**
     * Gets the name of this file, if any. For example:
     * <pre>
     *     "/a/b" ->  "b"
     *     "/a"   ->  "a"
     *     "/"    ->  null
     *     "a"    ->  "a"
     *     ""     ->  null
     * </pre>
     */
    @Nullable // TODO old code expect not null
    public abstract Name name();

    public abstract boolean isHidden();

    /**
     * Returns true if the {@code prefix} is an ancestor of this path,
     * or equal to this path.
     */
    public abstract boolean startsWith(Path prefix);

    /**
     * Returns a path by replace the prefix {@code oldPrefix} with
     * {@code newPrefix}. For example
     * <pre>
     * "/a/b".rebase("/a", "/hello") -> "/hello/b"
     * </pre>
     *
     * @throws IllegalArgumentException if {@code !this.startsWith(oldPrefix)}
     */
    public abstract Path rebase(Path oldPrefix, Path newPrefix);

    @Override
    public int describeContents() {
        return 0;
    }

    public abstract void setPermissions(Set<Permission> permissions)
            throws IOException;

    public abstract void setLastModifiedTime(LinkOption option, Instant instant)
            throws IOException;

    public abstract Stat stat(LinkOption option)
            throws IOException;

    /**
     * Creates this file and any missing parents as directories. This will
     * throw the same exceptions as {@link Path#createDir()} except
     * will not error if already exists as a directory.
     */
    public Path createDirs() throws IOException {
        try {
            if (stat(NOFOLLOW).isDirectory()) {
                return this;
            }
        } catch (FileNotFoundException ignore) {
        }

        Path parent = parent();
        if (parent != null) {
            parent.createDirs();
        }

        try {
            createDir();
        } catch (AlreadyExist ignore) {
        }

        return this;
    }

    public abstract Path createDir()
            throws IOException;

    /**
     * Creates directory with specified permissions,
     * the set of permissions with be restricted so
     * the resulting permissions may not be the same.
     */
    public abstract Path createDir(Set<Permission> permissions)
            throws IOException;

    public abstract Path createFile()
            throws IOException;

    /**
     * @param target the target the link will point to
     */
    public abstract Path createSymbolicLink(Path target)
            throws IOException;

    public abstract Path readSymbolicLink()
            throws IOException;

    /**
     * Moves this file tree to destination, destination must not exist.
     * <p>
     * If this is a link, the link itself is moved, link target file is
     * unaffected.
     */
    public abstract void move(Path destination)
            throws IOException;

    public abstract void delete()
            throws IOException;

    public abstract boolean exists(LinkOption option)
            throws IOException;

    /**
     * Returns true if this file is readable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public abstract boolean isReadable()
            throws IOException;

    /**
     * Returns true if this file is writable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public abstract boolean isWritable()
            throws IOException;

    /**
     * Returns true if this file is executable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public abstract boolean isExecutable()
            throws IOException;

    /**
     * Observes on this file for change events.
     * <p>
     * If this file is a directory, adding/removing immediate children and
     * any changes to the content/attributes of immediate children of this
     * directory will be notified, this is true for existing children as well as
     * newly added items after observation started.
     * <p>
     * Note that by the time a listener is notified, the target file may
     * have already be changed again, therefore a robust application should have
     * an alternative way of handling instead of reply on this fully.
     * <p>
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
            LinkOption option,
            Observer observer,
            Consumer childrenConsumer,
            @Nullable String logTag,
            int watchLimit
    ) throws IOException, InterruptedException;

    public Observation observe(
            LinkOption option,
            BatchObserver batchObserver,
            Consumer childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit,
            boolean quickNotifyFirstEvent,
            String tag,
            int watchLimit
    ) throws IOException, InterruptedException {

        return new BatchObserverNotifier(
                batchObserver,
                batchInterval,
                batchInternalUnit,
                quickNotifyFirstEvent,
                tag,
                watchLimit
        ).start(this, option, childrenConsumer);
    }

    public abstract void list(LinkOption option, Consumer consumer)
            throws IOException;


    public <C extends Collection<? super Path>> C list(
            final LinkOption option,
            final C collection
    ) throws IOException {
        list(option, new Consumer() {
            @Override
            public boolean accept(Path path) throws IOException {
                collection.add(path);
                return true;
            }
        });
        return collection;
    }

    public void traverse(
            LinkOption option,
            TraversalCallback<? super Path> visitor
    ) throws IOException {
        traverse(option, visitor, null);
    }

    /**
     * Performs a depth first traverse of this tree.
     * <p>
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
            LinkOption option,
            TraversalCallback<? super Path> visitor,
            @Nullable Comparator childrenComparator
    ) throws IOException {

        new Traverser(this, option, visitor, childrenComparator).traverse();
    }

    public abstract InputStream newInputStream()
            throws IOException;

    public abstract OutputStream newOutputStream(boolean append)
            throws IOException;

    public interface Consumer {
        /**
         * @return true to continue, false to stop for multi-item callbacks
         */
        boolean accept(Path path) throws IOException;
    }
}
