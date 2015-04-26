package l.files.fs;

import android.os.Parcelable;

import com.google.common.net.MediaType;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Represents a file system resource, such as a file or directory. Two resources
 * are equal if their URIs are equal.
 */
public interface Resource extends Parcelable {

    /**
     * The normalized/absolute URI of this resource. Every resource has only one
     * path and one uri representation, regardless whether or not it exists on
     * the file system. For example, a traditional file URI for a directory may
     * or may not end with a "/" depending on its existence, this is disallowed
     * for implementations of this interface, as that's two representations for
     * the resource.
     */
    URI getUri();

    /**
     * Gets the path of this resource. The returned path is only valid within
     * the context of the underlying file system.
     */
    String getPath();

    /**
     * Gets the name of this resource, or empty if this is the root resource.
     */
    String getName();

    /**
     * Gets the parent resource, returns null if this is the root resource.
     */
    @Nullable
    Resource getParent();

    /**
     * Resolves the given name/path relative to this resource.
     */
    Resource resolve(String other);

    /**
     * Returns a resource with the given parent replaced.
     * <p/>
     * e.g.
     * <pre>
     * Resource("/a/b").resolve(Resource("/a"), Resource("/c")) =
     * Resource("/c/b")
     * </pre>
     *
     * @throws IllegalArgumentException if {@code !this.startsWith(fromParent)}
     */
    Resource resolveParent(Resource fromParent, Resource toParent);

    /**
     * True if this resource is equal to or a descendant of the given resource.
     */
    boolean startsWith(Resource prefix);

    /**
     * True if this resource is considered a hidden resource.
     */
    boolean isHidden();

    /**
     * Returns true if exists, returns false if does not exist or failed to
     * determine existence.
     */
    boolean exists(LinkOption option);

    /**
     * Returns true if this resource is readable, return false if this resource
     * is not readable or failed to determine.
     */
    boolean isReadable();

    /**
     * Returns true if this resource is writable, return false if this resource
     * is not writable or failed to determine.
     */
    boolean isWritable();

    /**
     * Returns true if this resource is executable, return false if this
     * resource is not executable or failed to determine.
     */
    boolean isExecutable();

    /**
     * Observes on this resource for change events.
     * <p/>
     * If this resource is a directory, adding/removing immediate children and
     * any changes to the content/attributes of immediate children of this
     * directory will be notified, this is true for existing children as well as
     * newly added items after observation started.
     * <p/>
     * Note that by the time a listener is notified, the target resource may
     * have already be changed again, therefore a robust application should have
     * an alternative way of handling instead of reply on this fully.
     *
     * @param option if option is {@link LinkOption#NOFOLLOW} and this resource
     *               is a link, observe on the link instead of the link target
     */
    Closeable observe(LinkOption option, WatchEvent.Listener observer)
            throws IOException;

    /**
     * Performs a traversal that will terminate as soon as an error is
     * encountered.
     */
    void traverse(LinkOption option,
                  @Nullable ResourceVisitor pre,
                  @Nullable ResourceVisitor post) throws IOException;

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
     * pre.accept(a)
     * pre.accept(b)
     * post.accept(b)
     * pre.accept(c)
     * post.accept(c)
     * post.accept(a)
     * </pre>
     *
     * @param option  applies to root only, child links are never followed
     * @param pre     callback for pre order traversals
     * @param post    callback for post order traversals
     * @param handler handles any exception encountered duration traversal
     */
    void traverse(LinkOption option,
                  @Nullable ResourceVisitor pre,
                  @Nullable ResourceVisitor post,
                  @Nullable ResourceExceptionHandler handler) throws IOException;

    /**
     * Lists the immediate children of this resource.
     *
     * @throws AccessException       no permission to list
     * @throws NotExistException     the underlying resource does not exist
     * @throws NotDirectoryException target resource is not a directory, or if
     *                               option is {@link LinkOption#NOFOLLOW} and
     *                               the underlying resource is a symbolic link
     * @throws IOException           other failures
     */
    void list(LinkOption option, ResourceVisitor visitor) throws IOException;

    /**
     * List the children into the given collection. Returns the collection.
     *
     * @see #list(LinkOption, ResourceVisitor)
     */
    <T extends Collection<? super Resource>> T list(LinkOption option, T collection)
            throws IOException;

    /**
     * Returns the children.
     *
     * @see #list(LinkOption, ResourceVisitor)
     */
    List<Resource> list(LinkOption option) throws IOException;

    /**
     * Opens an input stream to the underlying file.
     * <p/>
     *
     * @throws AccessException   does not have permission to read
     * @throws NotExistException this resource does not exist
     * @throws NotFileException  this resource is not a file, or option is
     *                           {@link LinkOption#NOFOLLOW} and the underlying
     *                           resource is a symbolic link
     * @throws IOException       other failures
     */
    InputStream openInputStream(LinkOption option) throws IOException;

    /**
     * Equivalent to {@link #openOutputStream(LinkOption, boolean)
     * openOutputStream(option, false)}.
     */
    OutputStream openOutputStream(LinkOption option) throws IOException;

    /**
     * Opens an output stream to the underlying file.
     *
     * @throws AccessException   no write permission
     * @throws NotExistException parent resource does not exist
     * @throws NotFileException  this resource is a directory, or option is
     *                           {@link LinkOption#NOFOLLOW} and the underlying
     *                           resource is a symbolic link
     * @throws IOException       other failures
     */
    OutputStream openOutputStream(LinkOption option, boolean append)
            throws IOException;

    Reader openReader(LinkOption option, Charset charset) throws IOException;

    Writer openWriter(LinkOption option, Charset charset) throws IOException;

    // Writer openWriter(Charset charset, boolean append) throws IOException;

    /**
     * Creates this resource as a directory. Will fail if the directory already
     * exists.
     *
     * @return this
     * @throws AccessException       does not have permission to create
     * @throws ExistsException       this resource already exists
     * @throws NotExistException     a directory component in the path does not
     * @throws NotDirectoryException parent is not a directory
     * @throws IOException           other failures
     */
    Resource createDirectory() throws IOException;

    /**
     * Creates this resource and any missing parents as directories. This will
     * throw the same exceptions as {@link #createDirectory()} except will not
     * error if already exists as a directory.
     *
     * @return this
     */
    Resource createDirectories() throws IOException;

    /**
     * Creates the underlying resource as a file.
     *
     * @return this
     * @throws AccessException       does not have permission to create
     * @throws ExistsException       the underlying resource already exits
     * @throws NotExistException     the parent resource does not exist
     * @throws NotDirectoryException parent is not a directory
     * @throws IOException           other failures
     */
    Resource createFile() throws IOException;

    /**
     * Creates this resource as a file and creates any missing parents. This
     * will throw the same exceptions as {@link #createDirectory()} except will
     * not error if already exists.
     *
     * @return this
     */
    Resource createFiles() throws IOException;

    /**
     * Creates the underlying resource as a symbolic link to point to the given
     * location.
     *
     * @return this
     * @throws AccessException       does not have permission to create
     * @throws ExistsException       the underlying resource already exists
     * @throws NotExistException     this resource does not exist
     * @throws NotDirectoryException parent is not a directory
     * @throws IOException           other failures
     */
    Resource createSymbolicLink(Resource target) throws IOException;

    /**
     * If this is a symbolic link, returns the target file.
     *
     * @throws AccessException       does not have permission to read
     * @throws InvalidException      this is not a symbolic link
     * @throws NotExistException     this resource does not exist
     * @throws NotDirectoryException parent is not a directory
     * @throws IOException           other failures
     */
    Resource readSymbolicLink() throws IOException;

    /**
     * Reads the status of this resource.
     *
     * @throws AccessException   does not have permission to read status
     * @throws NotExistException this resource does not exist
     * @throws IOException       other failures
     */
    ResourceStatus readStatus(LinkOption option) throws IOException;

    /**
     * Moves this resource tree to the given destination, destination must not
     * exist.
     * <p/>
     * If this is a symbolic link, the link itself is moved, link target
     * resource is unaffected.
     *
     * @throws AccessException      does not have permission to rename
     * @throws NotExistException    this resource does not exist
     * @throws ExistsException      destination exists
     * @throws InvalidException     attempt to make a directory a subdirectory
     *                              of itself
     * @throws CrossDeviceException attempt to move to a different device
     * @throws IOException          other failures
     */
    void moveTo(Resource dst) throws IOException;

    /**
     * Deletes this resource.
     *
     * @throws AccessException   does not have permission to delete
     * @throws NotExistException this resource does not exist
     * @throws NotEmptyException this is a non empty directory
     * @throws IOException       other failures
     */
    void delete() throws IOException;

    /**
     * Updates the access time for this resource.
     *
     * @throws AccessException   does not have permission to update
     * @throws NotExistException this resource does not exist
     * @throws IOException       other failures
     */
    void setAccessTime(LinkOption option, Instant instant) throws IOException;

    /**
     * Updates the modification time for this resource.
     *
     * @throws AccessException   does not have permission to update
     * @throws NotExistException this resource does not exist
     * @throws IOException       other failures
     */
    void setModificationTime(LinkOption option, Instant instant) throws IOException;

    /**
     * Sets the permissions of this resource, this replaces the existing
     * permissions, not add.
     * <p/>
     * If this is a symbolic link, the permission of the target resource will be
     * changed, the permission of the link itself cannot be changed.
     *
     * @throws AccessException               does not have permission to update
     * @throws NotExistException             this resource does not exist
     * @throws IOException                   other failures
     * @throws UnsupportedOperationException if this is a symbolic link
     */
    void setPermissions(Set<Permission> permissions) throws IOException;

    /**
     * Detects the media type of the underlying file by reading it's content.
     */
    MediaType detectMediaType() throws IOException;

    /**
     * Reads the underlying file content as string.
     */
    String readString(LinkOption option, Charset charset) throws IOException;

    /**
     * Appends the underlying file content as string into the given appendable,
     * returns the appendable.
     */
    <T extends Appendable> T readString(LinkOption option,
                                        Charset charset,
                                        T appendable) throws IOException;

    /**
     * Overrides the content of this resource with the given content.
     */
    void writeString(LinkOption option, Charset charset, CharSequence content)
            throws IOException;

}
