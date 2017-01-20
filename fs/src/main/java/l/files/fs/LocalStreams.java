package l.files.fs;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import linux.ErrnoException;
import linux.Fcntl;
import linux.Unistd;

import static l.files.base.Throwables.addSuppressed;
import static l.files.fs.Stat.fstat;
import static linux.Errno.EISDIR;
import static linux.Fcntl.O_APPEND;
import static linux.Fcntl.O_CREAT;
import static linux.Fcntl.O_RDONLY;
import static linux.Fcntl.O_TRUNC;
import static linux.Fcntl.O_WRONLY;

final class LocalStreams {

    private LocalStreams() {
    }

    static FileInputStream newInputStream(Path path) throws IOException {

        int fd = newFd(path, O_RDONLY, 0);
        try {

            checkNotDirectory(fd);

            FileDescriptor descriptor = toFileDescriptor(fd);
            if (descriptor == null) {
                return new FileInputStream(path.toString());
            } else {
                return new LocalInputStream(descriptor, fd);
            }

        } catch (Throwable e) {
            try {
                Unistd.close(fd);
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

            FileDescriptor descriptor = toFileDescriptor(fd);
            if (descriptor == null) {
                return new FileOutputStream(path.toString());
            } else {
                return new LocalOutputStream(descriptor, fd);
            }

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

            Stat stat = fstat(fd);
            if (stat.isDirectory()) {
                throw new ErrnoException(EISDIR);
            }

        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e);
        }
    }

    private static FileDescriptor toFileDescriptor(int fd) {

        try {
            FileDescriptor descriptor = new FileDescriptor();
            Method setter = FileDescriptor.class.getMethod("setInt$", int.class);
            setter.setAccessible(true);
            setter.invoke(descriptor, fd);
            return descriptor;
        } catch (NoSuchMethodException ignored) {
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException ignored) {
        }

        try {
            FileDescriptor descriptor = new FileDescriptor();
            Field field = FileDescriptor.class.getField("descriptor");
            field.setAccessible(true);
            field.set(descriptor, fd);
            return descriptor;
        } catch (NoSuchFieldException ignored) {
        } catch (IllegalAccessException ignored) {
        }

        return null;
    }

    private static void close(InvalidateFd instance) throws IOException {
        // Make sure we don't close someone else's FD
        // on subsequent calls
        int closingFd = instance.getFd();
        instance.setFd(-1);

        if (closingFd == -1) {
            return;
        }

        try {
            Unistd.close(closingFd);
        } catch (ErrnoException e) {
            throw ErrnoExceptions.toIOException(e);
        }
    }

    private interface InvalidateFd {

        int getFd();

        void setFd(int fd);

    }


    private static final class LocalInputStream extends FileInputStream implements InvalidateFd {

        private int fd;

        LocalInputStream(FileDescriptor descriptor, int fd) {
            super(descriptor);
            this.fd = fd;
        }

        @Override
        public void close() throws IOException {
            super.close();
            LocalStreams.close(this);
        }

        @Override
        public int getFd() {
            return fd;
        }

        @Override
        public void setFd(int fd) {
            this.fd = fd;
        }

    }

    private static final class LocalOutputStream extends FileOutputStream implements InvalidateFd {

        private int fd;

        LocalOutputStream(FileDescriptor descriptor, int fd) {
            super(descriptor);
            this.fd = fd;
        }


        @Override
        public void close() throws IOException {
            super.close();
            LocalStreams.close(this);
        }

        @Override
        public int getFd() {
            return fd;
        }

        @Override
        public void setFd(int fd) {
            this.fd = fd;
        }
    }

}
