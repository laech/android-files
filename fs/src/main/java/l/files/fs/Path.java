package l.files.fs;

import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import l.files.base.Optional;
import l.files.fs.exception.*;
import linux.ErrnoException;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Paths;
import java.nio.file.attribute.*;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.reverse;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

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

    public static Path of(File file) {
        return new Path(file.toPath());
    }

    public static Path of(java.nio.file.Path path) {
        return new Path(path);
    }

    public static Path of(String path) {
        return new Path(Paths.get(path));
    }

    public static Path of(byte[] path) {
        return of(new String(path, UTF_8));
    }

    final java.nio.file.Path delegate;

    private Path(java.nio.file.Path delegate) {
        this.delegate = requireNonNull(delegate);
    }

    public byte[] toByteArray() {
        return toString().getBytes(UTF_8);
    }

    @Override
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

    public Path concat(String path) {
        return concat(of(path));
    }

    public Path concat(byte[] path) {
        return concat(of(path));
    }

    public Path getFileName() {
        java.nio.file.Path fileName = delegate.getFileName();
        return fileName == null ? null : new Path(fileName);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(getFileName()).map(Path::toString);
    }

    /**
     * The name part without extension.
     * <pre>
     *  base.ext  ->  base
     *  base      ->  base
     *  base.     ->  base.
     * .base.ext  -> .base
     * .base      -> .base
     * .base.     -> .base.
     * .          -> .
     * ..         -> ..
     * </pre>
     */
    public Optional<String> getBaseName() {
        return getName()
            .map(name -> name.lastIndexOf(".") == 0
                ? name
                : FilenameUtils.getBaseName(name));
    }

    /**
     * The extension part without base name.
     * <pre>
     *  base.ext  ->  ext
     * .base.ext  ->  ext
     *  base      ->  ""
     *  base.     ->  ""
     * .base      ->  ""
     * .base.     ->  ""
     * .          ->  ""
     * ..         ->  ""
     * </pre>
     */
    public Optional<String> getExtension() {
        return getName()
            .map(name -> name.lastIndexOf(".") == 0
                ? null
                : FilenameUtils.getExtension(name))
            .filter(it -> !it.isEmpty());
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

    public void setPermissions(Set<PosixFilePermission> permissions)
        throws IOException {
        Files.setPosixFilePermissions(delegate, permissions);
    }

    public void setLastModifiedTime(FileTime time)
        throws IOException {
        Files.setLastModifiedTime(delegate, time);
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

    public <A extends BasicFileAttributes> A readAttributes(
        Class<A> type,
        java.nio.file.LinkOption... options
    ) throws IOException {
        return Files.readAttributes(delegate, type, options);
    }

    public <V extends FileAttributeView> V getFileAttributeView(
        Class<V> type,
        java.nio.file.LinkOption... options
    ) {
        return Files.getFileAttributeView(delegate, type, options);
    }

    public Path createDirectory() throws IOException {
        Files.createDirectory(delegate);
        return this;
    }

    public Path createDirectory(Set<PosixFilePermission> permissionsHint)
        throws IOException {
        Files.createDirectory(
            delegate,
            PosixFilePermissions.asFileAttribute(permissionsHint)
        );
        return this;
    }

    public Path createDirectories() throws IOException {
        Files.createDirectories(delegate);
        return this;
    }

    public Path createFile() throws IOException {
        Files.createFile(delegate);
        return this;
    }

    public Path createSymbolicLink(Path target) throws IOException {
        Files.createSymbolicLink(delegate, target.delegate);
        return this;
    }

    public Path readSymbolicLink() throws IOException {
        return new Path(Files.readSymbolicLink(delegate));
    }

    public void move(Path destination, CopyOption... options)
        throws IOException {
        Files.move(delegate, destination.delegate, options);
    }

    public void delete() throws IOException {
        Files.delete(delegate);
    }

    public boolean exists(java.nio.file.LinkOption... options) {
        return Files.exists(delegate, options);
    }

    public boolean isReadable() {
        return Files.isReadable(delegate);
    }

    public boolean isWritable() {
        return Files.isWritable(delegate);
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
     */
    @Deprecated
    public void traverse(
        LinkOption option,
        TraversalCallback<? super Path> visitor,
        @Nullable Comparator<? super Path> childrenComparator
    ) throws IOException {

        new Traverser(this, option, visitor, childrenComparator).traverse();
    }

    @Deprecated
    public void traverse(
        LinkOption option,
        TraversalCallback<? super Path> visitor
    ) throws IOException {
        traverse(option, visitor, null);
    }

    public ParcelFileDescriptor newInputFileDescriptor() throws IOException {
        return ParcelFileDescriptor.open(
            delegate.toFile(),
            ParcelFileDescriptor.MODE_READ_ONLY
        );
    }

    public InputStream newInputStream() throws IOException {
        return Files.newInputStream(delegate);
    }

    public OutputStream newOutputStream(OpenOption... options)
        throws IOException {
        return Files.newOutputStream(delegate, options);
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
