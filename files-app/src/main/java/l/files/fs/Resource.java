package l.files.fs;

import android.os.Parcelable;

import com.google.common.net.MediaType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface Resource extends PathEntry, Parcelable {

    /**
     * Gets the name of this resource, or empty if this is the root
     */
    String getName();

    /**
     * Returns the watch service for the underlying file system.
     */
    WatchService getWatcher();

    /**
     * Returns true if this resource exists, returns false if this resource does
     * not exist or failed to determine existence.
     */
    boolean exists();

    /**
     * Resolves the given name/path relative to this resource.
     */
    Resource resolve(String other);

    /**
     * Traverse this subtree. Accepts an error handler, if the handler does not
     * rethrow the exception, traversal will continue.
     */
    // TODO ResourceStream?
    Iterable<Resource> traverse(
            TraversalOrder order,
            TraversalExceptionHandler handler) throws IOException;

    /**
     * Opens a resource stream to iterate through the immediate children.
     */
    ResourceStream<Resource> openResourceStream() throws IOException;

    InputStream openInputStream() throws IOException;

    OutputStream openOutputStream() throws IOException;

    /**
     * Creates the underlying resource as a directory.
     */
    void createDirectory() throws IOException;

    /**
     * Creates the underlying resource as a file.
     */
    void createFile() throws IOException;

    /**
     * Creates the underlying resource as a symbolic link to point to the given
     * location.
     */
    void createSymbolicLink(Path target) throws IOException;

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
    void move(Path dst) throws IOException;

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

}
