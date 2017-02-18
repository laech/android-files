package linux;

import java.io.IOException;

import javax.annotation.Nullable;

import l.files.fs.Path;
import l.files.fs.exception.AccessDenied;
import l.files.fs.exception.AlreadyExist;
import l.files.fs.exception.CrossDevice;
import l.files.fs.exception.DirectoryNotEmpty;
import l.files.fs.exception.FileSystemReadOnly;
import l.files.fs.exception.InvalidArgument;
import l.files.fs.exception.IsDirectory;
import l.files.fs.exception.NameTooLong;
import l.files.fs.exception.NoSuchEntry;
import l.files.fs.exception.NotDirectory;
import l.files.fs.exception.TooManySymbolicLinks;

import static linux.Errno.EACCES;
import static linux.Errno.EEXIST;
import static linux.Errno.EINVAL;
import static linux.Errno.EISDIR;
import static linux.Errno.ELOOP;
import static linux.Errno.ENAMETOOLONG;
import static linux.Errno.ENOENT;
import static linux.Errno.ENOTDIR;
import static linux.Errno.ENOTEMPTY;
import static linux.Errno.EPERM;
import static linux.Errno.EROFS;
import static linux.Errno.EXDEV;

public final class ErrnoException extends Exception {

    public final int errno;

    private String strerror;

    public ErrnoException(int errno) {
        super();
        this.errno = errno;
    }

    @Override
    public String getMessage() {
        if (strerror == null) {
            strerror = Str.strerror(errno);
        }
        return strerror;
    }

    public IOException toIOException(Path path) {
        return toIOException(path.toString());
    }

    public IOException toIOException(@Nullable String message) {
        if (errno == EPERM) return new AccessDenied(message, this);
        if (errno == EACCES) return new AccessDenied(message, this);
        if (errno == EEXIST) return new AlreadyExist(message, this);
        if (errno == ELOOP) return new TooManySymbolicLinks(message, this);
        if (errno == ENAMETOOLONG) return new NameTooLong(message, this);
        if (errno == ENOENT) return new NoSuchEntry(message, this);
        if (errno == ENOTDIR) return new NotDirectory(message, this);
        if (errno == EISDIR) return new IsDirectory(message, this);
        if (errno == ENOTEMPTY) return new DirectoryNotEmpty(message, this);
        if (errno == EINVAL) return new InvalidArgument(message, this);
        if (errno == EXDEV) return new CrossDevice(message, this);
        if (errno == EROFS) return new FileSystemReadOnly(message, this);
        return new IOException(message, this);
    }

}
