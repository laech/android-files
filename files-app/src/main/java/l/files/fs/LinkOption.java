package l.files.fs;

/**
 * Determines how file system operations should handle symbolic links.
 */
public enum LinkOption {

  /**
   * Follow symbolic links.
   */
  FOLLOW,

  /**
   * Do not follow symbolic links.
   */
  NO_FOLLOW

}
