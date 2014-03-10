package l.files.os;

public final class Os {
  private Os() {}

  static {
    System.loadLibrary("os");
  }

  /**
   * Gets the inode number of the file at the given path.
   *
   * @return the inode, or -1 if failed
   */
  public static native long inode(String path);

  /**
   * Creates a symbolic link.
   *
   * @param srcPath the path of the file
   * @param dstPath the path to the link
   * @return true if successful, false otherwise
   */
  public static native boolean symlink(String srcPath, String dstPath);

}
