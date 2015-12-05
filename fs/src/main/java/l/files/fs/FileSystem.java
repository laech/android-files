package l.files.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileSystem {

    Stat stat(Path path, LinkOption option) throws IOException;

    void createDir(Path path) throws IOException;

    void createFile(Path path) throws IOException;

    /**
     * @param target the target the link will point to
     * @param link   the link itself
     */
    void createLink(Path target, Path link) throws IOException;

    Path readLink(Path path) throws IOException;

    boolean exists(Path path, LinkOption option) throws IOException;

    boolean isReadable(Path path) throws IOException;

    boolean isWritable(Path path) throws IOException;

    boolean isExecutable(Path path) throws IOException;

    InputStream newInputStream(Path path) throws IOException;

    OutputStream newOutputStream(Path path, boolean append) throws IOException;
}
