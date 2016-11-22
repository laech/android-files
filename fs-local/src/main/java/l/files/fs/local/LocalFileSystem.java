package l.files.fs.local;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import l.files.fs.FileSystem;
import l.files.fs.Instant;
import l.files.fs.LinkOption;
import l.files.fs.Observation;
import l.files.fs.Observer;
import l.files.fs.Path;
import l.files.fs.Permission;
import l.files.fs.Stat;
import linux.Dirent;
import linux.Dirent.DIR;
import linux.ErrnoException;
import linux.Fcntl;
import linux.Stdio;
import linux.Unistd;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;
import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Permission.GROUP_EXECUTE;
import static l.files.fs.Permission.GROUP_READ;
import static l.files.fs.Permission.GROUP_WRITE;
import static l.files.fs.Permission.OTHERS_EXECUTE;
import static l.files.fs.Permission.OTHERS_READ;
import static l.files.fs.Permission.OTHERS_WRITE;
import static l.files.fs.Permission.OWNER_EXECUTE;
import static l.files.fs.Permission.OWNER_READ;
import static l.files.fs.Permission.OWNER_WRITE;
import static linux.Errno.EACCES;
import static linux.Errno.EAGAIN;
import static linux.Fcntl.O_CREAT;
import static linux.Fcntl.O_DIRECTORY;
import static linux.Fcntl.O_EXCL;
import static linux.Fcntl.O_NOFOLLOW;
import static linux.Fcntl.O_RDWR;
import static linux.Fcntl.open;
import static linux.Stat.S_IRGRP;
import static linux.Stat.S_IROTH;
import static linux.Stat.S_IRUSR;
import static linux.Stat.S_IRWXU;
import static linux.Stat.S_IWGRP;
import static linux.Stat.S_IWOTH;
import static linux.Stat.S_IWUSR;
import static linux.Stat.S_IXGRP;
import static linux.Stat.S_IXOTH;
import static linux.Stat.S_IXUSR;
import static linux.Stat.chmod;
import static linux.Stat.mkdir;

public final class LocalFileSystem extends Native implements FileSystem {

    public static final LocalFileSystem INSTANCE = new LocalFileSystem();

    private static final Map<Permission, Integer> PERMISSION_BITS = permissionsToBits();

    private static Map<Permission, Integer> permissionsToBits() {
        Map<Permission, Integer> map = new EnumMap<>(Permission.class);
        map.put(OWNER_READ, S_IRUSR);
        map.put(OWNER_WRITE, S_IWUSR);
        map.put(OWNER_EXECUTE, S_IXUSR);
        map.put(GROUP_READ, S_IRGRP);
        map.put(GROUP_WRITE, S_IWGRP);
        map.put(GROUP_EXECUTE, S_IXGRP);
        map.put(OTHERS_READ, S_IROTH);
        map.put(OTHERS_WRITE, S_IWOTH);
        map.put(OTHERS_EXECUTE, S_IXOTH);
        return unmodifiableMap(map);
    }

    public static Set<Permission> permissionsFromMode(int mode) {
        Set<Permission> permissions = EnumSet.noneOf(Permission.class);
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

    private static int permissionsToMode(Set<Permission> permissions) {
        int mode = 0;
        for (Permission permission : permissions) {
            mode |= PERMISSION_BITS.get(permission);
        }
        return mode;
    }

    @Override
    public void setPermissions(Path path, Set<Permission> permissions)
            throws IOException {

        try {
            chmod(path.toByteArray(), permissionsToMode(permissions));
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    @Override
    public void setLastModifiedTime(Path path, LinkOption option, Instant instant)
            throws IOException {


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
    public void stat(Path path, LinkOption option, Stat buffer) throws IOException {
        LocalStat.stat(path, option, (LocalStat) buffer);
    }

    @Override
    public LocalStat stat(Path path, LinkOption option) throws IOException {
        return LocalStat.stat(path, option);
    }

    @Override
    public LocalStat newEmptyStat() {
        return new LocalStat();
    }

    @Override
    public void createDir(Path path) throws IOException {
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            mkdir(path.toByteArray(), S_IRWXU);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    @Override
    public void createDir(Path path, Set<Permission> permissions) throws IOException {
        try {
            mkdir(path.toByteArray(), permissionsToMode(permissions));
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
        // Same flags and mode as java.io.File.createNewFile() on Android
        int flags = O_RDWR | O_CREAT | O_EXCL;
        int mode = S_IRUSR | S_IWUSR;
        int fd = open(path.toByteArray(), flags, mode);
        Unistd.close(fd);
    }

    @Override
    public void createSymbolicLink(Path link, Path target) throws IOException {
        try {

            Unistd.symlink(target.toByteArray(), link.toByteArray());

        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, target, link);
        }
    }

    @Override
    public Path readSymbolicLink(Path path) throws IOException {
        try {
            byte[] link = Unistd.readlink(path.toByteArray());
            return LocalPath.of(link);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    @Override
    public void move(Path src, Path dst) throws IOException {
        try {

            Stdio.rename(src.toByteArray(), dst.toByteArray());

        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, src, dst);
        }
    }

    @Override
    public void delete(Path path) throws IOException {
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
        return accessible(path, Unistd.R_OK);
    }

    @Override
    public boolean isWritable(Path path) throws IOException {
        return accessible(path, Unistd.W_OK);
    }

    @Override
    public boolean isExecutable(Path path) throws IOException {
        return accessible(path, Unistd.X_OK);
    }

    private boolean accessible(Path path, int mode) throws IOException {
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
            Consumer<? super Path> childrenConsumer,
            @Nullable String logTag,
            int watchLimit)
            throws IOException, InterruptedException {

        LocalObservable observable = new LocalObservable(path, observer, logTag);
        observable.start(option, childrenConsumer, watchLimit);
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
            Path path,
            LinkOption option,
            boolean dirOnly,
            Consumer<? super Path> consumer) throws IOException {

        int flags = O_DIRECTORY;
        if (option == NOFOLLOW) {
            flags |= O_NOFOLLOW;
        }

        try {
            int fd = Fcntl.open(path.toByteArray(), flags, 0);
            DIR dir = Dirent.fdopendir(fd);
            try {
                Dirent entry = new Dirent();
                while ((entry = Dirent.readdir(dir, entry)) != null) {
                    if (isSelfOrParent(entry)) {
                        continue;
                    }
                    if (dirOnly && (entry.d_type != Dirent.DT_DIR)) {
                        continue;
                    }
                    byte[] name = Arrays.copyOfRange(entry.d_name, 0, entry.d_name_len);
                    if (!consumer.accept(path.concat(name))) {
                        break;
                    }
                }
            } finally {
                Dirent.closedir(dir);
            }
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    static boolean isSelfOrParent(Dirent entry) {
        if (entry.d_name_len == 1 && entry.d_name[0] == '.') {
            return true;
        }
        if (entry.d_name_len == 2 && entry.d_name[0] == '.' && entry.d_name[1] == '.') {
            return true;
        }
        return false;
    }

    @Override
    public InputStream newInputStream(Path path) throws IOException {
        return LocalStreams.newInputStream(path);
    }

    @Override
    public OutputStream newOutputStream(Path path, boolean append) throws IOException {
        return LocalStreams.newOutputStream(path, append);
    }

}
