package l.files.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

public interface FileSystem {

    void setPermissions(Path path, Set<Permission> permissions)
            throws IOException;

    void setLastModifiedTime(Path path, LinkOption option, Instant instant)
            throws IOException;

    Stat stat(Path path, LinkOption option) throws IOException;

    void createDir(Path path) throws IOException;

    void createFile(Path path) throws IOException;

    /**
     * @param target the target the link will point to
     * @param link   the link itself
     */
    void createLink(Path target, Path link) throws IOException;

    Path readLink(Path path) throws IOException;

    void move(Path src, Path dst) throws IOException;

    void delete(Path path) throws IOException;

    boolean exists(Path path, LinkOption option) throws IOException;

    boolean isReadable(Path path) throws IOException;

    boolean isWritable(Path path) throws IOException;

    boolean isExecutable(Path path) throws IOException;

    Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer<? super Path> childrenConsumer)
            throws IOException, InterruptedException;

    InputStream newInputStream(Path path) throws IOException;

    OutputStream newOutputStream(Path path, boolean append) throws IOException;

    interface Consumer<E> {
        void accept(E entry) throws IOException;
    }
}
