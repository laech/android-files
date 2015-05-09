package l.files.fs.local;

import com.google.common.base.Joiner;

import java.io.IOException;

import l.files.fs.AccessDenied;
import l.files.fs.AlreadyExists;
import l.files.fs.DirectoryNotEmpty;
import l.files.fs.InvalidOperation;
import l.files.fs.NotDirectory;
import l.files.fs.NotExist;
import l.files.fs.Resource;
import l.files.fs.ResourceException;
import l.files.fs.UnsupportedOperation;

import static android.system.OsConstants.EACCES;
import static android.system.OsConstants.EEXIST;
import static android.system.OsConstants.EINVAL;
import static android.system.OsConstants.ELOOP;
import static android.system.OsConstants.ENOENT;
import static android.system.OsConstants.ENOTDIR;
import static android.system.OsConstants.ENOTEMPTY;
import static android.system.OsConstants.EXDEV;
import static l.files.fs.LinkOption.NOFOLLOW;

public final class ErrnoException extends Exception
{
    private final int errno;

    public ErrnoException(final int errno, final String msg)
    {
        super(msg);
        this.errno = errno;
    }

    public int errno()
    {
        return errno;
    }

    IOException toIOException(final String... paths)
    {
        return toIOException(this, errno, paths);
    }

    static IOException toIOException(
            final android.system.ErrnoException e,
            final String... paths)
    {
        return toIOException(e, e.errno, paths);
    }

    static IOException toIOException(
            final Exception cause,
            final int errno,
            final String... paths)
    {
        final String path = Joiner.on(", ").join(paths);
        if (errno == EACCES) return new AccessDenied(path, cause);
        if (errno == EEXIST) return new AlreadyExists(path, cause);
        if (errno == ENOENT) return new NotExist(path, cause);
        if (errno == ENOTDIR) return new NotDirectory(path, cause);
        if (errno == EINVAL) return new InvalidOperation(path, cause);
        if (errno == EXDEV) return new UnsupportedOperation(path, cause);
        if (errno == ENOTEMPTY) return new DirectoryNotEmpty(path, cause);
        return new ResourceException(path, cause);
    }

    /**
     * If the code that caused this exception has no follow symbolic link set,
     * this will check to see if this exception is caused by that. Returns false
     * is unable to determine.
     */
    boolean isCausedByNoFollowLink(final Resource resource)
    {
        // See for example open() linux system call
        return isCausedByNoFollowLink(errno, resource);
    }

    /**
     * @see #isCausedByNoFollowLink(Resource)
     */
    static boolean isCausedByNoFollowLink(
            final android.system.ErrnoException e,
            final Resource resource)
    {
        return isCausedByNoFollowLink(e.errno, resource);
    }

    private static boolean isCausedByNoFollowLink(
            final int errno,
            final Resource res)
    {
        try
        {
            // See for example open() linux system call
            return errno == ELOOP && res.stat(NOFOLLOW).isSymbolicLink();
        }
        catch (final IOException e)
        {
            return false;
        }
    }

}
