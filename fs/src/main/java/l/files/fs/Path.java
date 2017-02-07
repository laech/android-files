package l.files.fs;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import l.files.fs.event.BatchObserver;
import l.files.fs.event.BatchObserverNotifier;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;
import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.AlreadyExist;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;

import static com.google.common.base.Charsets.UTF_8;
import static l.files.fs.LinkOption.NOFOLLOW;

public abstract class Path implements Parcelable {

    public static final Creator<Path> CREATOR = new Creator<Path>() {

        @Override
        public Path createFromParcel(Parcel source) {
            return Path.of(source.createByteArray());
        }

        @Override
        public Path[] newArray(int size) {
            return new Path[size];
        }
    };

    static final Charset ENCODING = UTF_8;

    public static Path of(File file) {
        return of(file.getPath());
    }

    public static Path of(String path) {
        return of(path.getBytes(ENCODING));
    }

    public static Path of(byte[] path) {
        RelativePath result = new RelativePath(getNames(path));
        boolean absolute = path.length > 0 && path[0] == '/';
        return absolute ? new AbsolutePath(result) : result;
    }

    private static ImmutableList<Name> getNames(byte[] path) {
        ImmutableList.Builder<Name> names = ImmutableList.builder();
        for (int start = 0, end; start < path.length; start = end + 1) {
            end = ArrayUtils.indexOf(path, (byte) '/', start);
            if (end == -1) {
                end = path.length;
            }
            if (end > start) {
                names.add(new Name(path, start, end));
            }
        }
        return names.build();
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toByteArray(out);
        return out.toByteArray();
    }

    public abstract void toByteArray(ByteArrayOutputStream out);

    /**
     * Returns a string representation of this path.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        toByteArray(out);
        try {
            return out.toString(ENCODING.name());
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Converts this path to a URI,
     * this method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    public Uri toUri() {
        return Uri.fromFile(new File(toString()));
    }

    /**
     * If this is a relative path, converts it to an absolute path by
     * concatenating the current working directory with this path.
     * If this path is already an absolute path returns this.
     */
    public abstract Path toAbsolutePath();

    @Override
    public int hashCode() {
        return Arrays.hashCode(toByteArray()); // TODO
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Path &&
                Arrays.equals(toByteArray(), ((Path) o).toByteArray());
    }

    /**
     * Concatenates {@code path} onto the end of this path.
     */
    public abstract Path concat(Path path);

    public Path concat(Name name) {
        return concat(name.toPath());
    }

    public Path concat(String path) {
        return concat(of(path));
    }

    public Path concat(byte[] path) {
        return concat(of(path));
    }

    /**
     * Gets all the file names of this path. For example:
     * <pre>
     *     "/a/b/c" -> ["a", "b", "c"]
     * </pre>
     */
    public abstract ImmutableList<Name> names();

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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(toByteArray());
    }

    public void setPermissions(Set<Permission> permissions)
            throws IOException {
        FileSystem.INSTANCE.setPermissions(this, permissions);
    }

    public void setLastModifiedTime(LinkOption option, Instant instant)
            throws IOException {
        FileSystem.INSTANCE.setLastModifiedTime(this, option, instant);
    }

    public Stat stat(LinkOption option) throws IOException {
        return FileSystem.INSTANCE.stat(this, option);
    }

    /**
     * @throws AccessDenied         parent directory does not allow write
     *                              permission, or one of the ancestor
     *                              directory does not allow search permission
     * @throws AlreadyExist         an entry already exists at this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          one of the ancestors does not exist
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws IOException          other errors
     */
    public Path createDirectory() throws IOException {
        FileSystem.INSTANCE.createDirectory(this);
        return this;
    }

    /**
     * Creates directory with specified permissions,
     * the set of permissions with be restricted so
     * the resulting permissions may not be the same.
     */
    public Path createDirectory(Set<Permission> permissions)
            throws IOException {
        FileSystem.INSTANCE.createDirectory(this, permissions);
        return this;
    }

    /**
     * Creates this file and any missing parents as directories. This will
     * throw the same exceptions as {@link Path#createDirectory()} except
     * will not error if already exists as a directory.
     */
    public Path createDirectories() throws IOException {
        try {
            if (stat(NOFOLLOW).isDirectory()) {
                return this;
            }
        } catch (FileNotFoundException ignore) {
        }

        Path parent = parent();
        if (parent != null) {
            parent.createDirectories();
        }

        try {
            createDirectory();
        } catch (AlreadyExist ignore) { // TODO exists but is file?
        }

        return this;
    }

    /**
     * @throws AccessDenied         parent directory does not allow write
     *                              permission, or one of the ancestor
     *                              directory does not allow search permission
     * @throws AlreadyExist         an entry already exists at this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          one of the ancestors does not exist
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws IOException          other errors
     */
    public Path createFile() throws IOException {
        FileSystem.INSTANCE.createFile(this);
        return this;
    }

    /**
     * @param target the target the link will point to
     * @throws AccessDenied         parent directory does not allow write
     *                              permission, or one of the ancestor
     *                              directory does not allow search permission
     * @throws AlreadyExist         an entry already exists at this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          one of the ancestors does not exist
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws IOException          other errors
     */
    public Path createSymbolicLink(Path target) throws IOException {
        FileSystem.INSTANCE.createSymbolicLink(this, target);
        return this;
    }

    public Path readSymbolicLink() throws IOException {
        return FileSystem.INSTANCE.readSymbolicLink(this);
    }

    /**
     * Moves this file tree to destination, destination must not exist.
     * <p>
     * If this is a link, the link itself is moved, link target file is
     * unaffected.
     */
    public void move(Path destination) throws IOException {
        FileSystem.INSTANCE.move(this, destination);
    }

    public void delete() throws IOException {
        FileSystem.INSTANCE.delete(this);
    }

    public boolean exists(LinkOption option) throws IOException {
        return FileSystem.INSTANCE.exists(this, option);
    }

    /**
     * Returns true if this file is readable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public boolean isReadable() throws IOException {
        return FileSystem.INSTANCE.isReadable(this);
    }

    /**
     * Returns true if this file is writable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public boolean isWritable() throws IOException {
        return FileSystem.INSTANCE.isWritable(this);
    }

    /**
     * Returns true if this file is executable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public boolean isExecutable() throws IOException {
        return FileSystem.INSTANCE.isExecutable(this);
    }

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
    public Observation observe(
            LinkOption option,
            Observer observer,
            Consumer childrenConsumer,
            @Nullable String logTag,
            int watchLimit
    ) throws IOException, InterruptedException {

        return FileSystem.INSTANCE.observe(
                this,
                option,
                observer,
                childrenConsumer,
                logTag,
                watchLimit
        );
    }

    public Observation observe(
            LinkOption option,
            BatchObserver batchObserver,
            Path.Consumer childrenConsumer,
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

    public void list(LinkOption option, Consumer consumer) throws IOException {
        FileSystem.INSTANCE.list(this, option, consumer);
    }

    public <C extends Collection<? super Path>> C list(
            final LinkOption option,
            final C collection
    ) throws IOException {
        list(option, new Path.Consumer() {
            @Override
            public boolean accept(Path path) throws IOException {
                collection.add(path);
                return true;
            }
        });
        return collection;
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
            @Nullable Comparator<? super Path> childrenComparator
    ) throws IOException {

        new Traverser(this, option, visitor, childrenComparator).traverse();
    }

    public void traverse(
            LinkOption option,
            TraversalCallback<? super Path> visitor
    ) throws IOException {
        traverse(option, visitor, null);
    }

    public InputStream newInputStream() throws IOException {
        return FileSystem.INSTANCE.newInputStream(this);
    }

    public OutputStream newOutputStream(boolean append) throws IOException {
        return FileSystem.INSTANCE.newOutputStream(this, append);
    }

    public interface Consumer {
        /**
         * @return true to continue, false to stop for multi-item callbacks
         */
        boolean accept(Path path) throws IOException;
    }
}
