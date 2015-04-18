package l.files.fs;

import android.os.Parcelable;

import com.google.common.net.MediaType;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * Represents a file system resource, such as a file or directory. Two resources
 * are equal if their URIs are equal.
 * <p/>
 * If the underlying resource is a symbolic link, all operations will happen on
 * the symbolic link, not the link target resource.
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
     * Returns true if this resource exists, returns false if this resource does
     * not exist or failed to determine existence.
     */
    boolean exists();

    /**
     * Returns the watch service for the underlying file system.
     */
    @Deprecated
    WatchService getWatcher();

    // TODO
    Closeable observe(WatchEvent.Listener observer) throws IOException;

    /**
     * Traverse this subtree. Accepts an error handler, if the handler does not
     * rethrow the exception, traversal will continue.
     */
    Stream traverse(TraversalOrder order,
                    TraversalExceptionHandler handler) throws IOException;

    /**
     * Opens a resource stream to iterate through the immediate children.
     */
    Stream openDirectory() throws IOException;

    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream() throws IOException;

    OutputStream openOutputStream(boolean append) throws IOException;

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
    ResourceStatus readStatus(boolean followLink) throws IOException;

    /**
     * Renames this resource tree to the given destination.
     * <p/>
     * If this resource is non directory and destination exists as non
     * directory, destination will be replaced. If this resource is a directory
     * and destination is an empty directory, it will be replaced.
     *
     * @throws AccessException      does not have permission to rename
     * @throws NotExistException    this resource or the parent of the
     *                              destination does not exist
     * @throws NotEmptyException    destination is a non empty directory
     * @throws IsDirectoryException this resource is a not a directory and
     *                              destination exists as a directory
     * @throws InvalidException     attempt to make a directory a subdirectory
     *                              of itself
     * @throws CrossDeviceException attempt to move to a different device
     *                              (unsupported)
     * @throws IOException          other failures
     */
    void renameTo(Resource dst) throws IOException;

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
    void setAccessTime(Instant instant) throws IOException;

    /**
     * Updates the modification time for this resource.
     *
     * @throws AccessException   does not have permission to update
     * @throws NotExistException this resource does not exist
     * @throws IOException       other failures
     */
    void setModificationTime(Instant instant) throws IOException;

    /**
     * Sets the permissions of this resource, this replaces the existing
     * permissions, not add.
     *
     * @throws AccessException   does not have permission to update
     * @throws NotExistException this resource does not exist
     * @throws IOException       other failures
     */
    void setPermissions(Set<Permission> permissions) throws IOException;

    /**
     * Detects the media type of the underlying file by reading it's content.
     */
    MediaType detectMediaType() throws IOException;

    enum TraversalOrder {
        BREATH_FIRST,
        PRE_ORDER,
        POST_ORDER
    }

    interface TraversalExceptionHandler {
        void handle(Resource resource, IOException e);
    }

    interface Stream extends Iterable<Resource>, Closeable {
    }

}
