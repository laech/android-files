package l.files.fs.local;

import android.system.OsConstants;

import com.google.common.base.Joiner;

import java.io.IOException;

import l.files.fs.AccessException;
import l.files.fs.CrossDeviceException;
import l.files.fs.ExistsException;
import l.files.fs.InvalidException;
import l.files.fs.NotDirectoryException;
import l.files.fs.NotEmptyException;
import l.files.fs.NotExistException;
import l.files.fs.Resource;
import l.files.fs.ResourceException;

import static l.files.fs.LinkOption.NOFOLLOW;

public final class ErrnoException extends Exception {

    private static final long serialVersionUID = -1307818020659380579L;

    private final int errno;

    public ErrnoException(int errno, String msg) {
        super(msg);
        this.errno = errno;
    }

    public int errno() {
        return errno;
    }

    @Deprecated
    IOException toIOException() {
        return toIOException(this, errno);
    }

    IOException toIOException(String... paths) {
        return toIOException(this, errno, paths);
    }

    static IOException toIOException(android.system.ErrnoException e, String... paths) {
        return toIOException(e, e.errno, paths);
    }

    static IOException toIOException(Exception cause, int errno, String... paths) {
        String path = Joiner.on(", ").join(paths);
        if (errno == OsConstants.EACCES) return new AccessException(path, cause);
        if (errno == OsConstants.EEXIST) return new ExistsException(path, cause);
        if (errno == OsConstants.ENOENT) return new NotExistException(path, cause);
        if (errno == OsConstants.ENOTDIR) return new NotDirectoryException(path, cause);
        if (errno == OsConstants.EINVAL) return new InvalidException(path, cause);
        if (errno == OsConstants.EXDEV) return new CrossDeviceException(path, cause);
        if (errno == OsConstants.ENOTEMPTY) return new NotEmptyException(path, cause);
        return new ResourceException(path, cause);
    }

    /**
     * If the code that caused this exception has no follow symbolic link set,
     * this will check to see if this exception is caused by that. Returns false
     * is unable to determine.
     */
    boolean isCausedByNoFollowLink(Resource resource) {
        // See for example open() linux system call
        return isCausedByNoFollowLink(errno, resource);
    }

    /**
     * @see #isCausedByNoFollowLink(Resource)
     */
    static boolean isCausedByNoFollowLink(
            android.system.ErrnoException e, Resource resource) {
        return isCausedByNoFollowLink(e.errno, resource);
    }

    private static boolean isCausedByNoFollowLink(int errno, Resource res) {
        try {
            // See for example open() linux system call
            return errno == OsConstants.ELOOP
                    && res.stat(NOFOLLOW).isSymbolicLink();
        } catch (IOException e) {
            return false;
        }
    }

}
