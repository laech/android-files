package l.files.fs;

import java.io.IOException;

public interface FileSystem {

    Stat stat(Path path, LinkOption option) throws IOException;

    boolean exists(Path path, LinkOption option) throws IOException;

    boolean isReadable(Path path) throws IOException;

    boolean isWritable(Path path) throws IOException;

    boolean isExecutable(Path path) throws IOException;
}
