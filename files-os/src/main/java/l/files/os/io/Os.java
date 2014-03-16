package l.files.os.io;

public final class Os {
  private Os() {}

  static {
    System.loadLibrary("os");
  }

  /**
   * Get file status.
   * <p/>
   * Corresponds to C's <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/stat.html">stat()</a>
   * function.
   */
  public static native Stat stat(String path) throws OsException;

  /**
   * Get symbolic link status.
   * <p/>
   * Corresponds to C's <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/lstat.html">lstat()</a>
   * function.
   */
  public static native Stat lstat(String path) throws OsException;

  /**
   * Make a symbolic link to a file.
   * <p/>
   * Corresponds to C's <a href="http://pubs.opengroup.org/onlinepubs/000095399/functions/symlink.html">symlink()</a>
   * function.
   *
   * @param srcPath the path of the file
   * @param dstPath the path to the link
   */
  public static native void symlink(String srcPath, String dstPath) throws OsException;
}
