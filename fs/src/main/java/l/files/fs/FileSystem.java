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
     * @param link   the link itself
     * @param target the target the link will point to
     */
    void createLink(Path link, Path target) throws IOException;

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

    void list(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException;

    void listDirs(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException;

    void traverseSize(
            Path path,
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

    InputStream newInputStream(Path path) throws IOException;

    OutputStream newOutputStream(Path path, boolean append) throws IOException;

    interface Consumer<E> {
        /**
         * @return true to continue, false to stop for multi-item callbacks
         */
        boolean accept(E entry) throws IOException;
    }
}
