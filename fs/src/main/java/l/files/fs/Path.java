package l.files.fs;

import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.Parcelable;

import org.apache.commons.lang3.ArrayUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import l.files.fs.event.BatchObserver;
import l.files.fs.event.BatchObserverNotifier;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;
import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.AlreadyExist;
import l.files.fs.exception.CrossDevice;
import l.files.fs.exception.DirectoryNotEmpty;
import l.files.fs.exception.FileSystemReadOnly;
import l.files.fs.exception.InvalidArgument;
import l.files.fs.exception.IsDirectory;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;
import linux.ErrnoException;
import linux.Fcntl;
import linux.Stdio;
import linux.Unistd;

import static android.os.ParcelFileDescriptor.adoptFd;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static l.files.base.Throwables.addSuppressed;
import static l.files.base.io.Charsets.UTF_8;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Stat.S_IRUSR;
import static l.files.fs.Stat.S_IRWXU;
import static l.files.fs.Stat.S_IWUSR;
import static l.files.fs.Stat.chmod;
import static l.files.fs.Stat.fstat;
import static l.files.fs.Stat.mkdir;
import static linux.Errno.EISDIR;
import static linux.Fcntl.O_APPEND;
import static linux.Fcntl.O_CREAT;
import static linux.Fcntl.O_EXCL;
import static linux.Fcntl.O_RDONLY;
import static linux.Fcntl.O_RDWR;
import static linux.Fcntl.O_TRUNC;
import static linux.Fcntl.O_WRONLY;

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

    private static List<Name> getNames(byte[] path) {
        List<Name> names = new ArrayList<>();
        for (int start = 0, end; start < path.length; start = end + 1) {
            end = ArrayUtils.indexOf(path, (byte) '/', start);
            if (end == -1) {
                end = path.length;
            }
            if (end > start) {
                names.add(new Name(path, start, end));
            }
        }
        return unmodifiableList(names);
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
    public abstract List<Name> names();

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

    public List<Path> hierarchy() {
        List<Path> hierarchy = new ArrayList<>();
        for (Path p = this; p != null; p = p.parent()) {
            hierarchy.add(p);
        }
        reverse(hierarchy);
        return unmodifiableList(hierarchy);
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

    /**
     * @throws AccessDenied         search permission is denied for a parent path,
     *                              or the process is not privileged for this operation
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          file does not exist, or is empty
     * @throws NotDirectory         a parent path is not a directory
     * @throws FileSystemReadOnly   the underlying file system is read only
     * @throws IOException          other errors
     */
    public void setPermissions(Set<Permission> permissions) throws IOException {
        try {
            chmod(toByteArray(), Permission.toStatMode(permissions));
        } catch (ErrnoException e) {
            throw e.toIOException(this);
        }
    }

    /**
     * @throws AccessDenied         no permission to perform this operation
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          path does not exist, or is empty
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws FileSystemReadOnly   underlying file system is read only
     * @throws IOException          other errors
     */
    public void setLastModifiedTime(LinkOption option, Instant instant) throws IOException {
        try {
            byte[] pathBytes = toByteArray();
            long seconds = instant.seconds();
            int nanos = instant.nanos();
            boolean followLink = option == FOLLOW;
            Native.setModificationTime(pathBytes, seconds, nanos, followLink);
        } catch (ErrnoException e) {
            throw e.toIOException(this);
        }
    }

    /**
     * @throws AccessDenied         one of the ancestor directory does not
     *                              allow search permission
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          path does not exist, or is empty
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws IOException          other erros
     */
    public Stat stat(LinkOption option) throws IOException {
        return Stat.stat(this, option);
    }

    /**
     * @throws AccessDenied         parent directory does not allow write
     *                              permission, or one of the ancestor
     *                              directory does not allow search permission,
     *                              or the file system containing this path
     *                              does not support creation of directories
     * @throws AlreadyExist         an entry already exists at this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          one of the ancestors does not exist
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws FileSystemReadOnly   this path is on a read only file system
     * @throws IOException          other errors
     */
    public Path createDirectory() throws IOException {
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            mkdir(toByteArray(), S_IRWXU);
        } catch (ErrnoException e) {
            throw e.toIOException(this);
        }
        return this;
    }

    /**
     * Creates directory with specified permissions, the set of permissions
     * will be restricted so the resulting permissions may not be the same.
     *
     * @throws AccessDenied         parent directory does not allow write
     *                              permission, or one of the ancestor
     *                              directory does not allow search permission,
     *                              or the file system containing this path
     *                              does not support creation of directories
     * @throws AlreadyExist         an entry already exists at this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          one of the ancestors does not exist
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws FileSystemReadOnly   this path is on a read only file system
     * @throws IOException          other errors
     */
    public Path createDirectory(Set<Permission> permissionsHint) throws IOException {
        try {
            mkdir(toByteArray(), Permission.toStatMode(permissionsHint));
        } catch (ErrnoException e) {
            throw e.toIOException(this);
        }
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
            throw new AlreadyExist(
                    "Exists but not a directory: " + this, null);

        } catch (NoSuchEntry ignore) {
        }

        Path parent = parent();
        if (parent != null) {
            parent.createDirectories();
        }

        try {
            createDirectory();
        } catch (AlreadyExist e) {
            if (!stat(NOFOLLOW).isDirectory()) {
                throw new AlreadyExist(
                        "Exists but not a directory: " + this, null);
            }
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
     * @throws FileSystemReadOnly   this path is on a read only file system
     * @throws IOException          other errors
     */
    public Path createFile() throws IOException {
        try {

            // Same flags and mode as java.io.File.createNewFile() on Android
            int flags = O_RDWR | O_CREAT | O_EXCL;
            int mode = S_IRUSR | S_IWUSR;
            int fd = Fcntl.open(toByteArray(), flags, mode);
            Unistd.close(fd);

        } catch (ErrnoException e) {
            if (e.errno == EISDIR) {
                throw new AlreadyExist(toString(), e);
            }
            throw e.toIOException(this);
        }
        return this;
    }

    /**
     * @param target the target the link will point to
     * @throws AccessDenied         parent directory does not allow write
     *                              permission, or one of the ancestor
     *                              directory does not allow search permission,
     *                              or the file system containing this path
     *                              does not support creation of symolic links
     * @throws AlreadyExist         an entry already exists at this path
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          one of the ancestors does not exist
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws FileSystemReadOnly   this path is on a read only file system
     * @throws IOException          other errors
     */
    public Path createSymbolicLink(Path target) throws IOException {
        try {
            Unistd.symlink(target.toByteArray(), toByteArray());
        } catch (ErrnoException e) {
            throw e.toIOException(this + " -> " + target);
        }
        return this;
    }

    /**
     * @throws AccessDenied         one of the ancestor directory does not
     *                              have search permission
     * @throws InvalidArgument      if path is not a symbolic link
     * @throws NameTooLong          path name is too long
     * @throws NoSuchEntry          one of the ancestors does not exist
     * @throws NotDirectory         one of the ancestors is not a directory
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path
     * @throws IOException          other errors
     */
    public Path readSymbolicLink() throws IOException {
        try {
            byte[] link = Unistd.readlink(toByteArray());
            return of(link);
        } catch (ErrnoException e) {
            throw e.toIOException(this);
        }
    }

    /**
     * Renames this path to the destination.
     * <p>
     * Does nothing if source and destination are hard links to the same file.
     * <p>
     * If the destination exists, the following shows when this operation will
     * succeed or fail:
     * <pre>
     * okay: link -> link
     * okay: link -> file
     * fail: link -> directory
     *
     * okay: file -> link
     * okay: file -> file
     * okay: file -> directory
     *
     * fail: directory -> link
     * fail: directory -> file
     * fail: directory -> non empty directory
     * okay: directory -> empty directory
     * </pre>
     *
     * @throws AccessDenied         if anyone of the following is true
     *                              <ul>
     *                              <li>source parent directory is not writable</li>
     *                              <li>destination directory is not writable</li>
     *                              <li>one of source parent directories is not searchable</li>
     *                              <li>one of destination parent directories is not searchable</li>
     *                              <li>this path is a directory and is not writable</li>
     *                              <li>the file system containing this path does not
     *                              support renaming of the type request</li>
     *                              <li>this process is not privileged</li>
     *                              </ul>
     * @throws InvalidArgument      if destination is a subdirectory of this
     * @throws IsDirectory          if destination is an existing directory but
     *                              this path is not a directory
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     *                              when resolving this path or destination path
     * @throws NameTooLong          if this path name is too long,
     *                              or destination path name is too long
     * @throws NoSuchEntry          if anyone of the following is true
     *                              <ul>
     *                              <li>this path does not exist</li>
     *                              <li>one of the parent directories of destination does not exist</li>
     *                              <li>this path is empty</li>
     *                              <li>destination path is empty</li>
     *                              </ul>
     * @throws NotDirectory         if:
     *                              <ul>
     *                              <li>a parent path of this path is not a directory</li>
     *                              <li>a parent path of destination path is not a directory</li>
     *                              <li>this path is directory and destination exists but is not a directory</li>
     *                              </ul>
     * @throws DirectoryNotEmpty    if destination is a non-empty directory
     * @throws CrossDevice          source and destination are not on the same mounted file system
     * @throws FileSystemReadOnly   path is on a read only file system
     * @throws IOException          other errors
     */
    public void rename(Path destination) throws IOException {
        try {
            Stdio.rename(toByteArray(), destination.toByteArray());
        } catch (ErrnoException e) {
            throw e.toIOException(this + " -> " + destination);
        }
    }

    /**
     * @throws AccessDenied         parent directory does not allow write
     *                              permission, or one of the ancestor
     *                              directory does not allow search permission,
     *                              or this process is not privileged for this
     *                              action
     * @throws NameTooLong          path name is too long
     * @throws TooManySymbolicLinks too many symbolic links were encountered
     * @throws NotDirectory         if one of the parent path is not a directory
     * @throws NoSuchEntry          if one of the follow is true:
     *                              <ul>
     *                              <li>this path does not exist</li>
     *                              <li>one of the parent path is a dangling symbolic link</li>
     *                              <li>this path is empty</li>
     *                              </ul>
     * @throws FileSystemReadOnly   this path is on a read only file system
     * @throws IOException          other errors
     */
    public void delete() throws IOException {
        try {
            Stdio.remove(toByteArray());
        } catch (ErrnoException e) {
            throw e.toIOException(this);
        }
    }

    /**
     * Checks the existence of this file.
     * Throws the same exceptions as {@link #stat(LinkOption)}
     */
    public boolean exists(LinkOption option) throws IOException {
        try {
            stat(option);
            return true;
        } catch (NoSuchEntry e) {
            return false;
        }
    }

    /**
     * Returns true if this file is readable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public boolean isReadable() throws IOException {
        return accessible(this, Unistd.R_OK);
    }

    /**
     * Returns true if this file is writable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public boolean isWritable() throws IOException {
        return accessible(this, Unistd.W_OK);
    }

    /**
     * Returns true if this file is executable, return false if not.
     * <p>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    public boolean isExecutable() throws IOException {
        return accessible(this, Unistd.X_OK);
    }

    private boolean accessible(Path path, int mode) throws IOException {
        return Unistd.access(path.toByteArray(), mode) == 0;
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

        Observable observable = new Observable(this, observer, logTag);
        observable.start(option, childrenConsumer, watchLimit);
        return observable;
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

    /**
     * Lists the content of this directory, follows symbolic link.
     *
     * @throws AccessDenied no permission
     * @throws NoSuchEntry  directory does not exist,
     *                      or path is symbolic link but target directory does not exist,
     *                      or path is empty,
     * @throws NotDirectory if path is not a directory
     *                      and not a symbolic link to a directory
     * @throws IOException  other errors
     */
    public void list(Consumer consumer) throws IOException {
        try {
            FileSystem.INSTANCE.list(this, consumer);
        } catch (ErrnoException e) {
            throw e.toIOException(this);
        }
    }

    /**
     * Collects the content of this directory to the collection.
     *
     * @see #list(Consumer)
     */
    public <C extends Collection<? super Path>> C list(
            final C collection
    ) throws IOException {
        list(new Path.Consumer() {
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

        ParcelFileDescriptor fd = adoptFd(open(O_RDONLY, 0));
        try {

            checkNotDirectory(fd.getFd(), this);
            return new AutoCloseInputStream(fd);

        } catch (Throwable e) {
            try {
                fd.close();
            } catch (Throwable sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }
    }

    public OutputStream newOutputStream(boolean append) throws IOException {

        // Same flags and mode as java.io.FileOutputStream on Android
        int flags = O_WRONLY | O_CREAT | (append ? O_APPEND : O_TRUNC);

        // noinspection OctalInteger
        ParcelFileDescriptor fd = adoptFd(open(flags, 0600));
        try {

            checkNotDirectory(fd.getFd(), this);
            return new AutoCloseOutputStream(fd);

        } catch (Throwable e) {
            try {
                fd.close();
            } catch (Throwable sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }
    }

    private int open(int flags, int mode) throws IOException {
        try {
            return Fcntl.open(toByteArray(), flags, mode);
        } catch (ErrnoException e) {
            throw e.toIOException(this);
        }
    }

    private void checkNotDirectory(int fd, Path path) throws IOException {
        try {
            if (fstat(fd).isDirectory()) {
                throw new ErrnoException(EISDIR);
            }
        } catch (ErrnoException e) {
            throw e.toIOException(path);
        }
    }

    public interface Consumer {
        /**
         * @return true to continue, false to stop for multi-item callbacks
         */
        boolean accept(Path path) throws IOException;
    }

    private static final class Native extends l.files.fs.Native {

        private static native void setModificationTime(
                byte[] path,
                long seconds,
                int nanos,
                boolean followLink
        ) throws ErrnoException;
    }
}
