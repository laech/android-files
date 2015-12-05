package l.files.fs.local;

import java.io.FileNotFoundException;
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

    @Override
    public boolean exists(Path path, LinkOption option) throws IOException {
        try {
            // access() follows symbolic links
            // faccessat(AT_SYMLINK_NOFOLLOW) doesn't work on android
            // so use stat here
            stat(path, option);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        }
    }

}
