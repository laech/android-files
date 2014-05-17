package l.files.io.os;

/**
 * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/stdio.h.html">stdio.h</a>
 */
public final class Stdio extends Native {
  private Stdio() {}

  /**
   * @see <a href="http://pubs.opengroup.org/onlinepubs/7908799/xsh/rename.html">rename()</a>
   */
  public static native void rename(String oldPath, String newPath) throws ErrnoException;
}
