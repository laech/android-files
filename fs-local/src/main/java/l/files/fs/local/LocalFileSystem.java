package l.files.fs.local;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.FileSystem;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Path;
import l.files.fs.Permission;

import static java.util.Collections.unmodifiableSet;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.Permission.GROUP_EXECUTE;
import static l.files.fs.Permission.GROUP_READ;
import static l.files.fs.Permission.GROUP_WRITE;
import static l.files.fs.Permission.OTHERS_EXECUTE;
import static l.files.fs.Permission.OTHERS_READ;
import static l.files.fs.Permission.OTHERS_WRITE;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static l.files.fs.local.ErrnoException.EACCES;
import static l.files.fs.local.ErrnoException.EAGAIN;
import static l.files.fs.local.Fcntl.O_CREAT;
import static l.files.fs.local.Fcntl.O_EXCL;
import static l.files.fs.local.Fcntl.O_RDWR;
import static l.files.fs.local.Fcntl.open;
import static l.files.fs.local.Stat.S_IRGRP;
import static l.files.fs.local.Stat.S_IROTH;
import static l.files.fs.local.Stat.S_IRUSR;
import static l.files.fs.local.Stat.S_IRWXU;
import static l.files.fs.local.Stat.S_IWGRP;
import static l.files.fs.local.Stat.S_IWOTH;
import static l.files.fs.local.Stat.S_IWUSR;
import static l.files.fs.local.Stat.S_IXGRP;
import static l.files.fs.local.Stat.S_IXOTH;
import static l.files.fs.local.Stat.S_IXUSR;
import static l.files.fs.local.Stat.chmod;
import static l.files.fs.local.Stat.mkdir;
import static l.files.fs.local.Unistd.R_OK;
import static l.files.fs.local.Unistd.W_OK;
import static l.files.fs.local.Unistd.X_OK;
import static l.files.fs.local.Unistd.readlink;
import static l.files.fs.local.Unistd.symlink;

final class LocalFileSystem extends Native implements FileSystem {
    private LocalFileSystem() {
    }

    static final LocalFileSystem INSTANCE = new LocalFileSystem();

    private static final int[] PERMISSION_BITS = permissionsToBits();

    private static int[] permissionsToBits() {
        int[] bits = new int[9];
        bits[OWNER_READ.ordinal()] = S_IRUSR;
        bits[OWNER_WRITE.ordinal()] = S_IWUSR;
        bits[OWNER_EXECUTE.ordinal()] = S_IXUSR;
        bits[GROUP_READ.ordinal()] = S_IRGRP;
        bits[GROUP_WRITE.ordinal()] = S_IWGRP;
        bits[GROUP_EXECUTE.ordinal()] = S_IXGRP;
        bits[OTHERS_READ.ordinal()] = S_IROTH;
        bits[OTHERS_WRITE.ordinal()] = S_IWOTH;
        bits[OTHERS_EXECUTE.ordinal()] = S_IXOTH;
        return bits;
    }

    public static Set<Permission> permissionsFromMode(int mode) {
        Set<Permission> permissions = new HashSet<>(9);
        if ((mode & S_IRUSR) != 0) permissions.add(OWNER_READ);
        if ((mode & S_IWUSR) != 0) permissions.add(OWNER_WRITE);
        if ((mode & S_IXUSR) != 0) permissions.add(OWNER_EXECUTE);
        if ((mode & S_IRGRP) != 0) permissions.add(GROUP_READ);
        if ((mode & S_IWGRP) != 0) permissions.add(GROUP_WRITE);
        if ((mode & S_IXGRP) != 0) permissions.add(GROUP_EXECUTE);
        if ((mode & S_IROTH) != 0) permissions.add(OTHERS_READ);
        if ((mode & S_IWOTH) != 0) permissions.add(OTHERS_WRITE);
        if ((mode & S_IXOTH) != 0) permissions.add(OTHERS_EXECUTE);
        return unmodifiableSet(permissions);
    }

    @Override
    public void setPermissions(Path path, Set<Permission> permissions)
            throws IOException {

        int mode = 0;
        for (Permission permission : permissions) {
            mode |= PERMISSION_BITS[permission.ordinal()];
        }
        try {
            chmod(((LocalPath) path).toByteArray(), mode);
        } catch (ErrnoException e) {
            throw e.toIOException(path);
        }
    }

    @Override
    public void setLastModifiedTime(Path path, LinkOption option, Instant instant)
            throws IOException {

        try {
            byte[] pathBytes = ((LocalPath) path).toByteArray();
            long seconds = instant.seconds();
            int nanos = instant.nanos();
            boolean followLink = option == FOLLOW;
            setModificationTime(pathBytes, seconds, nanos, followLink);
        } catch (ErrnoException e) {
            throw e.toIOException(path);
        }
    }

    private static native void setModificationTime(
            byte[] path,
            long seconds,
            int nanos,
            boolean followLink) throws ErrnoException;

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
    public LocalPath readLink(Path path) throws IOException {
        try {
            byte[] link = readlink(((LocalPath) path).toByteArray());
            return LocalPath.of(link);
        } catch (ErrnoException e) {
            throw e.toIOException(path);
        }
    }

    @Override
    public void move(Path src, Path dst) throws IOException {
        try {

            Stdio.rename(
                    ((LocalPath) src).toByteArray(),
                    ((LocalPath) dst).toByteArray());

        } catch (ErrnoException e) {
            throw e.toIOException(src, dst);
        }
    }

    @Override
    public void delete(Path path) throws IOException {
        while (true) {
            try {
                Stdio.remove(((LocalPath) path).toByteArray());
                break;
            } catch (ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw e.toIOException(path);
                }
            }
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
    public Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer<? super Path> childrenConsumer)
            throws IOException, InterruptedException {

        LocalObservable observable = new LocalObservable(((LocalPath) path), observer);
        observable.start(option, childrenConsumer);
        return observable;
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
