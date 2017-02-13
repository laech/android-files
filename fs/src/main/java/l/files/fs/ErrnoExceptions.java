package l.files.fs;

import android.text.TextUtils;

import java.io.IOException;

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
import linux.ErrnoException;

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

final class ErrnoExceptions {

    private ErrnoExceptions() {
    }

    static IOException toIOException(ErrnoException cause, Object... paths) {
        String message = TextUtils.join(", ", paths);
        if (cause.errno == EPERM) return new AccessDenied(message, cause);
        if (cause.errno == EACCES) return new AccessDenied(message, cause);
        if (cause.errno == EEXIST) return new AlreadyExist(message, cause);
        if (cause.errno == ELOOP) return new TooManySymbolicLinks(message, cause);
        if (cause.errno == ENAMETOOLONG) return new NameTooLong(message, cause);
        if (cause.errno == ENOENT) return new NoSuchEntry(message, cause);
        if (cause.errno == ENOTDIR) return new NotDirectory(message, cause);
        if (cause.errno == EISDIR) return new IsDirectory(message, cause);
        if (cause.errno == ENOTEMPTY) return new DirectoryNotEmpty(message, cause);
        if (cause.errno == EINVAL) return new InvalidArgument(message, cause);
        if (cause.errno == EXDEV) return new CrossDevice(message, cause);
        if (cause.errno == EROFS) return new FileSystemReadOnly(message, cause);
        return new IOException(message, cause);
    }

}
