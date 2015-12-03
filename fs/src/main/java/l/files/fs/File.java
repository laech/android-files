package l.files.fs;

import android.os.Parcelable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Represents a file system file, such as a file or directory.
 */
public interface File extends Parcelable {

    String MEDIA_TYPE_OCTET_STREAM = "application/octet-stream";
    String MEDIA_TYPE_ANY = "*/*";

    Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    Charset UTF_8 = Charset.forName("UTF-8");

    URI uri();

    /**
     * Gets the path of this file. The returned path is only valid within
     * the context of the underlying file system.
     */
    Path path();

    /**
     * Gets the name of this file, or empty if this is the root file.
     */
    Name name();

    /**
     * Gets the parent file, or null.
     */
    File parent();

    /**
     * Gets the file hierarchy of this file.
     * <p/>
     * e.g. {@code "/a/b" -> ["/", "/a", "/a/b"]}
     */
    List<File> hierarchy();

    /**
     * Resolves the given path relative to this file.
     */
    File resolve(String other);

    /**
     * Resolves a child with the given name.
     */
    File resolve(Name other);

    /**
     * Returns a file with the given parent replaced.
     * <p/>
     * e.g.
     * <pre>
     * File("/a/b").resolve(File("/a"), File("/c")) =
     * File("/c/b")
     * </pre>
     *
     * @throws IllegalArgumentException if {@code !this.startsWith(fromParent)}
     */
    File rebase(File fromParent, File toParent);

    /**
     * True if this file is considered a hidden file.
     */
    boolean isHidden();

    boolean exists(LinkOption option) throws IOException;

    /**
     * Returns true if this file is readable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    boolean isReadable() throws IOException;

    /**
     * Returns true if this file is writable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    boolean isWritable() throws IOException;

    /**
     * Returns true if this file is executable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    boolean isExecutable() throws IOException;

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
     * @param option if option is {@link LinkOption#NOFOLLOW} and this file is a
     *               link, observe on the link instead of the link target
     */
    Observation observe(LinkOption option, Observer observer)
            throws IOException, InterruptedException;

    Observation observe(
            LinkOption option,
            Observer observer,
            FileConsumer childrenConsumer) throws IOException, InterruptedException;

    Observation observe(
            LinkOption option,
            BatchObserver batchObserver,
            FileConsumer childrenConsumer,
            long batchInterval,
            TimeUnit batchInternalUnit) throws IOException, InterruptedException;

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
     * visitor.onPost(b)
     * visitor.onPreVisit(c)
     * visitor.onPost(c)
     * visitor.onPost(a)
     * </pre>
     *
     * @param option applies to root only, child links are never followed
     */
    void traverse(LinkOption option, Visitor visitor) throws IOException;

    void traverseSize(
            LinkOption option,
            SizeVisitor accumulator) throws IOException;

    interface SizeVisitor {

        /**
         * Called per file/directory.
         *
         * @param size       the size of the file/directory in bytes
         * @param sizeOnDisk the size of actual storage used in bytes
         */
        boolean onSize(long size, long sizeOnDisk)
                throws IOException;

    }

    /**
     * @param childrenComparator option comparator to sort children
     */
    void traverse(LinkOption option, Visitor visitor, Comparator<File> childrenComparator) throws IOException;

    <C extends Collection<? super File>> C list(LinkOption option, C collection) throws IOException;

    <C extends Collection<? super File>> C listDirs(LinkOption option, C collection) throws IOException;

    void list(LinkOption option, Consumer consumer) throws IOException;

    void listDirs(LinkOption option, Consumer consumer) throws IOException;

    InputStream newInputStream() throws IOException;

    InputStream newBufferedInputStream() throws IOException;

    DataInputStream newBufferedDataInputStream() throws IOException;

    OutputStream newOutputStream() throws IOException;

    OutputStream newOutputStream(boolean append) throws IOException;

    OutputStream newBufferedOutputStream() throws IOException;

    DataOutputStream newBufferedDataOutputStream() throws IOException;

    String readAllUtf8() throws IOException;

    Reader newReader(Charset charset) throws IOException;

    Writer newWriter(Charset charset) throws IOException;

    Writer newWriter(Charset charset, boolean append) throws IOException;

    String readDetectingCharset(int limit) throws IOException;

    void writeAllUtf8(CharSequence content) throws IOException;

    void writeAll(CharSequence content, Charset charset) throws IOException;

    void appendUtf8(CharSequence content) throws IOException;

    void copyFrom(InputStream in) throws IOException;

    /**
     * Creates this file as a directory. Will fail if the directory already
     * exists.
     *
     * @return this
     */
    File createDir() throws IOException;

    /**
     * Creates this file and any missing parents as directories. This will
     * throw the same exceptions as {@link #createDir()} except will not
     * error if already exists as a directory.
     *
     * @return this
     */
    File createDirs() throws IOException;

    /**
     * Creates the underlying file as a file.
     *
     * @return this
     */
    File createFile() throws IOException;

    /**
     * Creates this file as a file and creates any missing parents. This
     * will throw the same exceptions as {@link #createFile()} except will not
     * error if already exists.
     *
     * @return this
     */
    File createFiles() throws IOException;

    /**
     * Creates the underlying file as a link to point to the given
     * location.
     *
     * @return this
     */
    File createLink(File target) throws IOException;

    /**
     * If this is a link, returns the target file.
     */
    File readLink() throws IOException;

    /**
     * Reads the status of this file.
     */
    Stat stat(LinkOption option) throws IOException;

    /**
     * Moves this file tree to the given destination, destination must not
     * exist.
     * <p/>
     * If this is a link, the link itself is moved, link target file is
     * unaffected.
     */
    void moveTo(File dst) throws IOException;

    /**
     * Deletes this file. Fails if this is a non-empty directory.
     */
    void delete() throws IOException;

    void deleteIfExists() throws IOException;

    void deleteRecursive() throws IOException;

    void deleteRecursiveIfExists() throws IOException;

    /**
     * Updates the modification time for this file.
     */
    void setLastModifiedTime(LinkOption option, Instant instant) throws IOException;

    /**
     * Sets the permissions of this file, this replaces the existing
     * permissions, not add.
     * <p/>
     * If this is a link, the permission of the target file will be changed,
     * the permission of the link itself cannot be changed.
     */
    void setPermissions(Set<Permission> permissions) throws IOException;

    /**
     * Removes the given permissions from this file's existing permissions.
     * <p/>
     * If this is a link, the permission of the target file will be changed,
     * the permission of the link itself cannot be changed.
     */
    void removePermissions(Set<Permission> permissions) throws IOException;

    /**
     * Detects the content type of this file based on its properties
     * without reading the content of this file.
     * Returns {@link #MEDIA_TYPE_OCTET_STREAM} if unknown.
     */
    String detectBasicMediaType(Stat stat) throws IOException;

    /**
     * Detects the content type of this file based on its content.
     * Returns {@link #MEDIA_TYPE_OCTET_STREAM} if unknown.
     */
    String detectContentMediaType(Stat stat) throws IOException;

    /**
     * Detects the content type of this file based on its properties
     * and its content.
     * Returns {@link #MEDIA_TYPE_OCTET_STREAM} if unknown.
     */
    String detectMediaType(Stat stat) throws IOException;

    interface Consumer {

        boolean accept(File file) throws IOException;

    }

}
