package l.files.fs;

import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import l.files.base.Optional;
import l.files.base.io.Charsets;
import l.files.fs.event.BatchObserver;
import l.files.fs.event.BatchObserverNotifier;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;
import l.files.fs.exception.*;
import linux.ErrnoException;
import linux.Fcntl;
import linux.Stdio;
import linux.Unistd;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static android.os.ParcelFileDescriptor.adoptFd;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;
import static l.files.base.Throwables.addSuppressed;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Stat.*;
import static linux.Errno.EISDIR;
import static linux.Fcntl.*;

@Deprecated
public class Path implements Parcelable, Comparable<Path> {

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

    static final Charset ENCODING = Charsets.UTF_8;

    public static Path of(File file) {
        return new Path(file.toPath());
    }

    public static Path of(String path) {
        return new Path(Paths.get(path));
    }

    public static Path of(byte[] path) {
        return of(new String(path, UTF_8));
    }

    private final java.nio.file.Path delegate;

    private Path(java.nio.file.Path delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public byte[] toByteArray() {
        return toString().getBytes(UTF_8);
    }

    public String toString() {
        return delegate.toString();
    }

    /**
     * Converts this path to a URI,
     * this method always replaces malformed-input and unmappable-character
     * sequences with some default replacement string.
     */
    public Uri toUri() {
        return Uri.fromFile(delegate.toFile());
    }

    public Path toAbsolutePath() {
        return new Path(delegate.toAbsolutePath());
    }

    public java.nio.file.Path toJavaPath() {
        return delegate;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Path &&
            delegate.equals(((Path) o).delegate);
    }

    /**
     * Concatenates {@code path} onto the end of this path.
     */
    public Path concat(Path path) {
        return new Path(Paths.get(toString(), path.toString()));
    }

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
    public Name name() {
        java.nio.file.Path name = delegate.getFileName();
        return name == null ? null : Name.of(name.toString());
    }

    public Path getFileName() {
        java.nio.file.Path fileName = delegate.getFileName();
        return fileName == null ? null : new Path(fileName);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(name()).map(name -> name.toString());
    }

    public Optional<String> getBaseName() {
        return Optional.ofNullable(name()).map(name -> name.base());
    }

    public Optional<String> getExtension() {
        return Optional.ofNullable(name()).map(name -> {
            String extension = name.extension();
            return "".equals(extension) ? null : extension;
        });
    }

    public Optional<String> getExtensionWithLeadingDot() {
        return getExtension().map(extension -> "." + extension);
    }

    @Nullable
    public Path parent() {
        java.nio.file.Path parent = delegate.getParent();
        return parent == null ? null : new Path(parent);
    }

    public List<Path> hierarchy() {
        List<Path> hierarchy = new ArrayList<>();
        for (Path p = this; p != null; p = p.parent()) {
            hierarchy.add(p);
        }
        reverse(hierarchy);
        return unmodifiableList(hierarchy);
    }

    public boolean isHidden() {
        java.nio.file.Path name = delegate.getFileName();
        return name != null && name.toString().startsWith(".");
    }

    public boolean isAbsolute() {
        return delegate.isAbsolute();
    }

    public boolean startsWith(Path prefix) {
        return delegate.startsWith(prefix.delegate);
    }

    /**
     * Returns a path by replace the prefix {@code oldPrefix} with
     * {@code newPrefix}. For example
     * <pre>
     * "/a/b".rebase("/a", "/hello") -> "/hello/b"
     * </pre>
     *
     * @throws IllegalArgumentException if {@code !this.startsWith(oldPrefix)}
     */
    public Path rebase(Path oldPrefix, Path newPrefix) {
        if (!startsWith(oldPrefix)) {
            throw new IllegalArgumentException("this=" + this +
                ", oldPrefix=" + oldPrefix +
                ", newPrefix" + newPrefix);
        }
        return new Path(newPrefix.delegate.resolve(
            oldPrefix.delegate.relativize(delegate)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(toByteArray());
    }

    /**
     * @throws AccessDenied         search permission is denied for a parent
     *                              path,
     *                              or the process is not privileged for this
     *                              operation
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
    public void setLastModifiedTime(LinkOption option, Instant instant)
        throws IOException {
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
    public Path createDirectory(Set<Permission> permissionsHint)
        throws IOException {
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
     *                              <li>source parent directory is not
     *                              writable</li>
     *                              <li>destination directory is not
     *                              writable</li>
     *                              <li>one of source parent directories is
     *                              not searchable</li>
     *                              <li>one of destination parent directories
     *                              is not searchable</li>
     *                              <li>this path is a directory and is not
     *                              writable</li>
     *                              <li>the file system containing this path
     *                              does not
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
     *                              <li>one of the parent directories of
     *                              destination does not exist</li>
     *                              <li>this path is empty</li>
     *                              <li>destination path is empty</li>
     *                              </ul>
     * @throws NotDirectory         if:
     *                              <ul>
     *                              <li>a parent path of this path is not a
     *                              directory</li>
     *                              <li>a parent path of destination path is
     *                              not a directory</li>
     *                              <li>this path is directory and
     *                              destination exists but is not a
     *                              directory</li>
     *                              </ul>
     * @throws DirectoryNotEmpty    if destination is a non-empty directory
     * @throws CrossDevice          source and destination are not on the
     *                              same mounted file system
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
     *                              <li>one of the parent path is a dangling
     *                              symbolic link</li>
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
        @Nullable String tag,
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
     *                      or path is symbolic link but target directory
     *                      does not exist,
     *                      or path is empty,
     * @throws NotDirectory if path is not a directory
     *                      and not a symbolic link to a directory
     * @throws IOException  other errors
     * @deprecated use {@link PathListKt#list(Path)} instead
     */
    @Deprecated
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
     * @deprecated use {@link PathListKt#list(Path)} instead
     */
    @Deprecated
    public <C extends Collection<? super Path>> C list(
        C collection
    ) throws IOException {
        list((Consumer) path -> {
            collection.add(path);
            return true;
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
     * @deprecated use {@link PathTraversalKt#traverse(Path, Function2)} instead
     */
    @Deprecated
    public void traverse(
        LinkOption option,
        TraversalCallback<? super Path> visitor,
        @Nullable Comparator<? super Path> childrenComparator
    ) throws IOException {

        new Traverser(this, option, visitor, childrenComparator).traverse();
    }

    /**
     * @deprecated use {@link PathTraversalKt#traverse(Path, Function2)} instead
     */
    @Deprecated
    public void traverse(
        LinkOption option,
        TraversalCallback<? super Path> visitor
    ) throws IOException {
        traverse(option, visitor, null);
    }

    public ParcelFileDescriptor newInputFileDescriptor() throws IOException {
        return ParcelFileDescriptor.adoptFd(open(O_RDONLY, 0));
    }

    public FileInputStream newInputStream() throws IOException {

        ParcelFileDescriptor fd = newInputFileDescriptor();
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

    public FileOutputStream newOutputStream(boolean append) throws IOException {

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

    @Override
    public int compareTo(Path o) {
        return delegate.compareTo(o.delegate);
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
