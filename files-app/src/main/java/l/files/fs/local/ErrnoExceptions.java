package l.files.fs.local;

import android.system.ErrnoException;
import android.text.TextUtils;

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

public final class ErrnoExceptions {
  private ErrnoExceptions() {
  }

  static IOException toIOException(ErrnoException e, String... paths) {
    return toIOException(e, e.errno, paths);
  }

  static IOException toIOException(Exception cause, int errno, String... paths) {
    String message = TextUtils.join(", ", paths);
    if (errno == EACCES) return new AccessDenied(message, cause);
    if (errno == EEXIST) return new AlreadyExists(message, cause);
    if (errno == ENOENT) return new NotExist(message, cause);
    if (errno == ENOTDIR) return new NotDirectory(message, cause);
    if (errno == EINVAL) return new InvalidOperation(message, cause);
    if (errno == EXDEV) return new UnsupportedOperation(message, cause);
    if (errno == ENOTEMPTY) return new DirectoryNotEmpty(message, cause);
    return new ResourceException(message, cause);
  }

  /**
   * If the code that caused this exception has no follow symbolic link set,
   * this will check to see if this exception is caused by that. Returns false
   * is unable to determine.
   */
  static boolean isCausedByNoFollowLink(ErrnoException e, Resource resource) {
    return isCausedByNoFollowLink(e.errno, resource);
  }

  private static boolean isCausedByNoFollowLink(int errno, Resource res) {
    try {
      // See for example open() linux system call
      return errno == ELOOP && res.stat(NOFOLLOW).isSymbolicLink();
    } catch (IOException e) {
      return false;
    }
  }

  public static native String strerror(int errno);

}
