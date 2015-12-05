package l.files.fs.local;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import l.files.fs.FileSystem;
import l.files.fs.LinkOption;
import l.files.fs.Path;

import static l.files.fs.local.ErrnoException.EACCES;
import static l.files.fs.local.Unistd.R_OK;
import static l.files.fs.local.Unistd.W_OK;
import static l.files.fs.local.Unistd.X_OK;

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

    @Override
    public boolean isReadable(Path path) throws IOException {
        return accessible(path, R_OK);
    }

    @Override
    public boolean isWritable(Path path) throws IOException {
        return accessible(path, W_OK);
    }

    @Override
    public boolean isExecutable(Path path) throws IOException {
        return accessible(path, X_OK);
    }

    private boolean accessible(Path path, int mode) throws IOException {
        try {
            Unistd.access(((LocalPath) path).toByteArray(), mode);
            return true;
        } catch (ErrnoException e) {
            if (e.errno == EACCES) {
                return false;
            }
            throw e.toIOException(path);
        }
    }

    @Override
    public InputStream newInputStream(Path path) throws IOException {
        return LocalStreams.newInputStream((LocalPath) path);
    }

    @Override
    public OutputStream newOutputStream(Path path, boolean append) throws IOException {
        return LocalStreams.newOutputStream((LocalPath) path, append);
    }

}
