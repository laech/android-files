package l.files.fs.local;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import l.files.fs.FileSystem;
import l.files.fs.LinkOption;
import l.files.fs.Path;

import static l.files.fs.local.ErrnoException.EACCES;
import static l.files.fs.local.Fcntl.O_CREAT;
import static l.files.fs.local.Fcntl.O_EXCL;
import static l.files.fs.local.Fcntl.O_RDWR;
import static l.files.fs.local.Fcntl.open;
import static l.files.fs.local.Stat.S_IRUSR;
import static l.files.fs.local.Stat.S_IRWXU;
import static l.files.fs.local.Stat.S_IWUSR;
import static l.files.fs.local.Stat.mkdir;
import static l.files.fs.local.Unistd.R_OK;
import static l.files.fs.local.Unistd.W_OK;
import static l.files.fs.local.Unistd.X_OK;
import static l.files.fs.local.Unistd.symlink;

enum LocalFileSystem implements FileSystem {

    INSTANCE;

    @Override
    public Stat stat(Path path, LinkOption option) throws IOException {
        return Stat.stat(((LocalPath) path), option);
    }

    @Override
    public void createDir(Path path) throws IOException {
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            mkdir(((LocalPath) path).toByteArray(), S_IRWXU);
        } catch (ErrnoException e) {
            throw e.toIOException(path);
        }
    }

    @Override
    public void createFile(Path path) throws IOException {
        try {
            createFileNative((LocalPath) path);
        } catch (ErrnoException e) {
            throw e.toIOException(path);
        }
    }

    private void createFileNative(LocalPath path) throws ErrnoException {
        // Same flags and mode as java.io.File.createNewFile() on Android
        int flags = O_RDWR | O_CREAT | O_EXCL;
        int mode = S_IRUSR | S_IWUSR;
        int fd = open(path.toByteArray(), flags, mode);
        Unistd.close(fd);
    }

    @Override
    public void createLink(Path target, Path link) throws IOException {
        try {

            symlink(((LocalPath) target).toByteArray(),
                    ((LocalPath) link).toByteArray());

        } catch (ErrnoException e) {
            throw e.toIOException(target, link);
        }
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
