package l.files.fs.local;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static l.files.fs.local.ErrnoException.EISDIR;
import static l.files.fs.local.ErrnoException.EPERM;
import static l.files.fs.local.Fcntl.O_APPEND;
import static l.files.fs.local.Fcntl.O_CREAT;
import static l.files.fs.local.Fcntl.O_NOATIME;
import static l.files.fs.local.Fcntl.O_RDONLY;
import static l.files.fs.local.Fcntl.O_TRUNC;
import static l.files.fs.local.Fcntl.O_WRONLY;
import static l.files.fs.local.Stat.S_ISDIR;
import static l.files.fs.local.Stat.fstat;

final class LocalStreams {

    private LocalStreams() {
    }

    static FileInputStream newInputStream(LocalFile file) throws IOException {

        int fd = newFd(file, O_RDONLY, 0);
        try {

            checkNotDirectory(fd);

            FileDescriptor descriptor = toFileDescriptor(fd);
            if (descriptor == null) {
                return new FileInputStream(file.file());
            } else {
                return new LocalInputStream(descriptor, fd);
            }

        } catch (Throwable e) {
            try {
                Unistd.close(fd);
            } catch (Throwable sup) {
                e.addSuppressed(sup);
            }
            throw e;
        }
    }

    static FileOutputStream newOutputStream(LocalFile file, boolean append) throws IOException {

        // Same flags and mode as java.io.FileOutputStream on Android

        int flags = O_WRONLY | O_CREAT;
        if (append) {
            flags |= O_APPEND;
        } else {
            flags |= O_TRUNC;
        }

        // noinspection OctalInteger
        int fd = newFd(file, flags, 0600);
        try {

            checkNotDirectory(fd);

            FileDescriptor descriptor = toFileDescriptor(fd);
            if (descriptor == null) {
                return new FileOutputStream(file.file());
            } else {
                return new LocalOutputStream(descriptor, fd);
            }

        } catch (Throwable e) {
            try {
                Unistd.close(fd);
            } catch (Throwable sup) {
                e.addSuppressed(sup);
            }
            throw e;
        }
    }


    private static int newFd(LocalFile file, int flags, int mode) throws IOException {

        // TODO make O_NOATIME an option?

        try {

            return Fcntl.open(file.path(), flags | O_NOATIME, mode);

        } catch (ErrnoException e) {
            // EPERM for No permission for O_NOATIME
            if (e.errno != EPERM) {
                throw e.toIOException(file.path());
            }
        }

        try {
            return Fcntl.open(file.path(), flags, mode);
        } catch (ErrnoException e) {
            throw e.toIOException(file.path());
        }

    }

    private static void checkNotDirectory(int fd) throws IOException {
        try {

            if (S_ISDIR(fstat(fd).mode())) {
                throw new ErrnoException(EISDIR);
            }

        } catch (ErrnoException e) {
            throw e.toIOException();
        }
    }

    private static FileDescriptor toFileDescriptor(int fd) {

        FileDescriptor descriptor = new FileDescriptor();

        try {
            Method setter = FileDescriptor.class.getMethod("setInt$", int.class);
            setter.setAccessible(true);
            setter.invoke(descriptor, fd);
            return descriptor;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }

        try {
            Field field = FileDescriptor.class.getField("descriptor");
            field.setAccessible(true);
            field.set(descriptor, fd);
            return descriptor;
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }

        return descriptor;
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
            throw e.toIOException();
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
