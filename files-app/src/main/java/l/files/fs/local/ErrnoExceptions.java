package l.files.fs.local;

import android.system.ErrnoException;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

import static android.system.OsConstants.ENOENT;

public final class ErrnoExceptions {
  private ErrnoExceptions() {
  }

  static IOException toIOException(ErrnoException e, String... paths) {
    return toIOException(e, e.errno, paths);
  }

  static IOException toIOException(Exception cause, int errno, String... paths) {
    String message = TextUtils.join(", ", paths);
    if (errno == ENOENT) {
      FileNotFoundException e = new FileNotFoundException(message);
      e.initCause(cause);
      return e;
    }
    return new IOException(message, cause);
  }

  public static native String strerror(int errno);

}
