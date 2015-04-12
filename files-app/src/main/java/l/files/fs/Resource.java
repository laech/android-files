package l.files.fs;

import android.os.Parcelable;

import com.google.common.net.MediaType;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

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

    // TODO replace WatchService
    // ResourceSubscription observe(ResourceObserver observer) throws IOException;

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

    /**
     * Creates this resource as a directory. Will fail if the directory already
     * exists.
     */
    void createDirectory() throws IOException;

    /**
     * Creates this resource and any missing parents as directories, will not
     * error if already exists.
     */
    void createDirectories() throws IOException;

    /**
     * Creates the underlying resource as a file.
     */
    void createFile() throws IOException;

    /**
     * Creates the underlying resource as a symbolic link to point to the given
     * location.
     */
    void createSymbolicLink(Resource target) throws IOException;

    /**
     * If this is a symbolic link, returns the target file.
     */
    Resource readSymbolicLink() throws IOException;

    /**
     * Reads the status of this resource.
     */
    ResourceStatus readStatus(boolean followLink) throws IOException;

    /**
     * Moves this resource tree to the given destination.
     */
    void move(Resource dst) throws IOException;

    /**
     * Deletes this resource. Will fail if this resource is a directory and not
     * empty.
     */
    void delete() throws IOException;

    void setLastModifiedTime(long time) throws IOException;

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
