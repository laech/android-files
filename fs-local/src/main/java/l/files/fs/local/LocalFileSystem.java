package l.files.fs.local;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import l.files.fs.FileSystem;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Path;
import l.files.fs.Permission;
import linux.ErrnoException;

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
import static linux.Errno.EACCES;
import static linux.Errno.EAGAIN;

public final class LocalFileSystem extends Native implements FileSystem {

    static {
        init();
    }

    private static native void init();

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
    public String scheme() {
        return "file";
    }

    @Override
    public Path path(URI uri) {
        if (!scheme().equals(uri.getScheme())) {
            throw new IllegalArgumentException(uri.toString());
        }
        return LocalPath.of(uri.getPath());
    }

    @Override
    public Path path(byte[] path) {
        return LocalPath.of(path);
    }

    private static void checkLocalPath(Path path) {
        if (!(path instanceof LocalPath)) {
            throw new IllegalArgumentException(path.toString());
        }
    }

    @Override
    public void setPermissions(Path path, Set<Permission> permissions)
            throws IOException {

        checkLocalPath(path);

        int mode = 0;
        for (Permission permission : permissions) {
            mode |= PERMISSION_BITS[permission.ordinal()];
        }
        try {
            chmod(path.toByteArray(), mode);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    @Override
    public void setLastModifiedTime(Path path, LinkOption option, Instant instant)
            throws IOException {

        checkLocalPath(path);

        try {
            byte[] pathBytes = path.toByteArray();
            long seconds = instant.seconds();
            int nanos = instant.nanos();
            boolean followLink = option == FOLLOW;
            setModificationTime(pathBytes, seconds, nanos, followLink);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    private static native void setModificationTime(
            byte[] path,
            long seconds,
            int nanos,
            boolean followLink) throws ErrnoException;

    @Override
    public Stat stat(Path path, LinkOption option) throws IOException {
        checkLocalPath(path);
        return Stat.stat(path, option);
    }

    @Override
    public void createDir(Path path) throws IOException {
        checkLocalPath(path);
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            mkdir(path.toByteArray(), S_IRWXU);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    @Override
    public void createFile(Path path) throws IOException {
        try {
            createFileNative(path);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    private void createFileNative(Path path) throws ErrnoException {
        checkLocalPath(path);
        // Same flags and mode as java.io.File.createNewFile() on Android
        int flags = O_RDWR | O_CREAT | O_EXCL;
        int mode = S_IRUSR | S_IWUSR;
        int fd = open(path.toByteArray(), flags, mode);
        Unistd.close(fd);
    }

    @Override
    public void createSymbolicLink(Path link, Path target) throws IOException {
        checkLocalPath(link);
        checkLocalPath(target);
        try {

            symlink(target.toByteArray(), link.toByteArray());

        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, target, link);
        }
    }

    @Override
    public Path readSymbolicLink(Path path) throws IOException {
        checkLocalPath(path);
        try {
            byte[] link = readlink(path.toByteArray());
            return LocalPath.of(link);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    @Override
    public void move(Path src, Path dst) throws IOException {
        checkLocalPath(src);
        checkLocalPath(dst);
        try {

            Stdio.rename(src.toByteArray(), dst.toByteArray());

        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, src, dst);
        }
    }

    @Override
    public void delete(Path path) throws IOException {
        checkLocalPath(path);
        while (true) {
            try {
                Stdio.remove(path.toByteArray());
                break;
            } catch (ErrnoException e) {
                if (e.errno != EAGAIN) {
                    throw ErrnoExceptions.toIOException(e, path);
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
        checkLocalPath(path);
        try {
            Unistd.access(path.toByteArray(), mode);
            return true;
        } catch (ErrnoException e) {
            if (e.errno == EACCES) {
                return false;
            }
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    @Override
    public Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer<? super Path> childrenConsumer)
            throws IOException, InterruptedException {

        checkLocalPath(path);
        LocalObservable observable = new LocalObservable(path, observer);
        observable.start(option, childrenConsumer);
        return observable;
    }

    @Override
    public void list(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException {

        list(path, option, false, consumer);
    }

    @Override
    public void listDirs(
            Path path,
            LinkOption option,
            Consumer<? super Path> consumer) throws IOException {

        list(path, option, true, consumer);
    }

    private void list(
            final Path path,
            final LinkOption option,
            final boolean dirOnly,
            final Consumer<? super Path> consumer) throws IOException {

        try {
            Dirent.list(
                    path.toByteArray(),
                    option == FOLLOW,
                    new Dirent.Callback() {

                        @Override
                        public boolean onNext(
                                byte[] nameBuffer,
                                int nameLength,
                                boolean isDirectory) throws IOException {

                            if (dirOnly && !isDirectory) {
                                return true;
                            }
                            byte[] name = Arrays.copyOf(nameBuffer, nameLength);
                            return consumer.accept(path.resolve(name));
                        }

                    });
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    @Override
    public void traverseSize(
            Path path,
            LinkOption option,
            SizeVisitor accumulator) throws IOException {

        try {
            traverseSize(path.toByteArray(), option == FOLLOW, accumulator);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, this);
        }

    }

    private static native void traverseSize(
            byte[] path,
            boolean followLink,
            SizeVisitor accumulator) throws ErrnoException;


    @Override
    public InputStream newInputStream(Path path) throws IOException {
        checkLocalPath(path);
        return LocalStreams.newInputStream(path);
    }

    @Override
    public OutputStream newOutputStream(Path path, boolean append) throws IOException {
        checkLocalPath(path);
        return LocalStreams.newOutputStream(path, append);
    }

}
