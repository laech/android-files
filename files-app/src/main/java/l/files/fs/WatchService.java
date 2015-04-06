package l.files.fs;


import java.io.Closeable;
import java.io.IOException;

/**
 * Service designed for monitoring file changes within a directory.
 * <p/>
 * When registering on a directory, all files/directories of the given directory
 * will be monitored for changes (non-recursive), including file
 * addition/modification/deletion and anything that changes the attribute of the
 * files/directories. This is non-recursive, meaning only changes to
 * files/directories at the current depth level will be reported.
 * <p/>
 * Note that by the time a listener is notified, the target file may have
 * already be changed, therefore a robust application should have an alternative
 * way of handling instead of reply on this fully.
 */
public interface WatchService extends Closeable {

    /**
     * Starts monitoring on the given path, file systems event on the given path
     * will be sent to the given registered listeners. Does nothing if the
     * listener is already registered for the given path. Note that if the path
     * is a directory, attribute change events of the directory itself will also
     * be reported.
     */
    void register(Path path, WatchEvent.Listener listener) throws IOException;

    /**
     * Stops monitoring on the given path. Does nothing if the listener is not
     * registered for the given path.
     */
    void unregister(Path path, WatchEvent.Listener listener);

    /**
     * Returns true if the given path is registered.
     */
    boolean isRegistered(Path path);

    /**
     * Returns true if the path can be watched, false otherwise.
     */
    boolean isWatchable(Path path);

}