package l.files.fs;

import java.io.IOException;

public interface FileSystem {

    Stat stat(Path path, LinkOption option) throws IOException;

}
