package l.files.fs;

import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import linux.ErrnoException;
import linux.Fcntl;
import linux.Unistd;

import static android.os.ParcelFileDescriptor.adoptFd;
import static l.files.base.Throwables.addSuppressed;
import static l.files.fs.Stat.fstat;
import static linux.Errno.EISDIR;
import static linux.Fcntl.O_APPEND;
import static linux.Fcntl.O_CREAT;
import static linux.Fcntl.O_RDONLY;
import static linux.Fcntl.O_TRUNC;
import static linux.Fcntl.O_WRONLY;
import static linux.Unistd.close;

final class Streams {

    private Streams() {
    }

    static FileInputStream newInputStream(Path path) throws IOException {

        int fd = newFd(path, O_RDONLY, 0);
        try {

            checkNotDirectory(fd);
            return new AutoCloseInputStream(adoptFd(fd));

        } catch (Throwable e) {
            try {
                close(fd);
            } catch (Throwable sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }
    }

    static FileOutputStream newOutputStream(Path path, boolean append) throws IOException {

        // Same flags and mode as java.io.FileOutputStream on Android

        int flags = O_WRONLY | O_CREAT;
        if (append) {
            flags |= O_APPEND;
        } else {
            flags |= O_TRUNC;
        }

        // noinspection OctalInteger
        int fd = newFd(path, flags, 0600);
        try {

            checkNotDirectory(fd);
            return new AutoCloseOutputStream(adoptFd(fd));

        } catch (Throwable e) {
            try {
                Unistd.close(fd);
            } catch (Throwable sup) {
                addSuppressed(e, sup);
            }
            throw e;
        }
    }

    private static int newFd(Path path, int flags, int mode) throws IOException {
        try {
            return Fcntl.open(path.toByteArray(), flags, mode);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e, path);
        }
    }

    private static void checkNotDirectory(int fd) throws IOException {
        try {

            if (fstat(fd).isDirectory()) {
                throw new ErrnoException(EISDIR);
            }

        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e);
        }
    }

}
