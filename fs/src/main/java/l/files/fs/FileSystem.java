package l.files.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nullable;

import l.files.fs.Path.Consumer;
import l.files.fs.event.Observation;
import l.files.fs.event.Observer;
import l.files.fs.exception.AlreadyExist;
import linux.Dirent;
import linux.Dirent.DIR;
import linux.ErrnoException;
import linux.Fcntl;
import linux.Stdio;
import linux.Unistd;

import static l.files.fs.LinkOption.FOLLOW;
import static l.files.fs.LinkOption.NOFOLLOW;
import static l.files.fs.Stat.S_IRUSR;
import static l.files.fs.Stat.S_IRWXU;
import static l.files.fs.Stat.S_IWUSR;
import static l.files.fs.Stat.chmod;
import static l.files.fs.Stat.mkdir;
import static linux.Errno.EAGAIN;
import static linux.Errno.EISDIR;
import static linux.Fcntl.O_CREAT;
import static linux.Fcntl.O_DIRECTORY;
import static linux.Fcntl.O_EXCL;
import static linux.Fcntl.O_NOFOLLOW;
import static linux.Fcntl.O_RDWR;
import static linux.Fcntl.open;

final class FileSystem extends Native {

    public static final FileSystem INSTANCE = new FileSystem();

    void setLastModifiedTime(Path path, LinkOption option, Instant instant)
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

    void createDirectory(Path path) throws IOException {
        try {
            // Same permission bits as java.io.File.mkdir() on Android
            mkdir(path.toByteArray(), S_IRWXU);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    void createDirectory(Path path, Set<Permission> permissionsHint)
            throws IOException {
        try {
            mkdir(path.toByteArray(), Permission.toStatMode(permissionsHint));
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    void createFile(Path path) throws IOException {
        try {
            createFileNative(path);
        } catch (ErrnoException e) {
            if (e.errno == EISDIR) {
                throw new AlreadyExist(path.toString(), e);
            }
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

    Path readSymbolicLink(Path path) throws IOException {
        try {
            byte[] link = Unistd.readlink(path.toByteArray());
            return Path.of(link);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    void rename(Path src, Path dst) throws IOException {
        try {

            Stdio.rename(src.toByteArray(), dst.toByteArray());

        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, src + " -> " + dst);
        }
    }

    void delete(Path path) throws IOException {
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

    boolean isReadable(Path path) throws IOException {
        return accessible(path, Unistd.R_OK);
    }

    boolean isWritable(Path path) throws IOException {
        return accessible(path, Unistd.W_OK);
    }

    boolean isExecutable(Path path) throws IOException {
        return accessible(path, Unistd.X_OK);
    }

    private boolean accessible(Path path, int mode) throws IOException {
        return Unistd.access(path.toByteArray(), mode) == 0;
    }

    Observation observe(
            Path path,
            LinkOption option,
            Observer observer,
            Consumer childrenConsumer,
            @Nullable String logTag,
            int watchLimit)
            throws IOException, InterruptedException {

        Observable observable = new Observable(path, observer, logTag);
        observable.start(option, childrenConsumer, watchLimit);
        return observable;
    }

    void list(
            Path path,
            LinkOption option,
            Consumer consumer) throws IOException {

        list(path, option, false, consumer);
    }

    private void list(
            Path path,
            LinkOption option,
            boolean dirOnly,
            Consumer consumer
    ) throws IOException {

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

    InputStream newInputStream(Path path) throws IOException {
        return Streams.newInputStream(path);
    }

    OutputStream newOutputStream(Path path, boolean append) throws IOException {
        return Streams.newOutputStream(path, append);
    }

}
