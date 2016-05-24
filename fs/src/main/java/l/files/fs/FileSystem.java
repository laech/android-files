package l.files.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

public interface FileSystem {

    String scheme();

    Path path(URI uri);

    Path path(byte[] path);

    void setPermissions(Path path, Set<Permission> permissions)
            throws IOException;

    void setLastModifiedTime(Path path, LinkOption option, Instant instant)
            throws IOException;

    void stat(Path path, LinkOption option, Stat buffer) throws IOException;

    Stat stat(Path path, LinkOption option) throws IOException;

    Stat newEmptyStat();

    void createDir(Path path) throws IOException;

    void createFile(Path path) throws IOException;

    /**
     * @param link   the link itself
     * @param target the target the link will point to
     */
    void createSymbolicLink(Path link, Path target) throws IOException;

    Path readSymbolicLink(Path path) throws IOException;

    /**
     * Moves src file tree to dst, destination must not exist.
     * <p/>
     * If src is a link, the link itself is moved, link target file is
     * unaffected.
     */
    void move(Path src, Path dst) throws IOException;

    void delete(Path path) throws IOException;

    boolean exists(Path path, LinkOption option) throws IOException;

    /**
     * Returns true if this file is readable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    boolean isReadable(Path path) throws IOException;

    /**
     * Returns true if this file is writable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    boolean isWritable(Path path) throws IOException;

    /**
     * Returns true if this file is executable, return false if not.
     * <p/>
     * If this is a link, returns the result for the link target, not the link
     * itself.
     */
    boolean isExecutable(Path path) throws IOException;

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
     * @param option           if option is {@link LinkOption#NOFOLLOW} and
     *                         this file is a link, observe on the link instead
     *                         of the link target
     * @param childrenConsumer consumer will be called for all immediate
     *                         children of {@code path}
     * @param logTag           tag for debug logging
     * @param watchLimit       limit the number of watch descriptors, or -1
     */
    Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer<? super Path> childrenConsumer,
            String logTag,
            int watchLimit)
            throws IOException, InterruptedException;

    void list(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException;

    void listDirs(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException;

    InputStream newInputStream(Path path) throws IOException;

    OutputStream newOutputStream(Path path, boolean append) throws IOException;

    interface Consumer<E> {
        /**
         * @return true to continue, false to stop for multi-item callbacks
         */
        boolean accept(E entry) throws IOException;
    }
}
