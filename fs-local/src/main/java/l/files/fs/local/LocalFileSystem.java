package l.files.fs.local;

import java.io.IOException;

import l.files.fs.FileSystem;
import l.files.fs.LinkOption;
import l.files.fs.Path;

enum LocalFileSystem implements FileSystem {

    INSTANCE;

    @Override
    public Stat stat(Path path, LinkOption option) throws IOException {
        return Stat.stat(((LocalPath) path), option);
    }

}
