package l.files.io.os;

/**
 * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/unistd.h.html">unistd.h</a>
 */
public final class Unistd extends Native {

  /* Macros for access() */
  public static final int R_OK = 4;  /* Read */
  public static final int W_OK = 2;  /* Write */
  public static final int X_OK = 1;  /* Execute */
  public static final int F_OK = 0;  /* Existence */

  private Unistd() {}

  /**
   * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/access.html">access()</a>
   */
  public static native void access(String path, int mode) throws ErrnoException;

  /**
   * @see <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/symlink.html">symlink()</a>
   */
  public static native void symlink(String srcPath, String dstPath) throws ErrnoException;

  /**
   * @see <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/readlink.html">readlink()</a>
   */
  public static native String readlink(String link) throws ErrnoException;
}
