package l.files.fs;

/**
 * Option to indicate how symbolic link should be handled when operating on a
 * file. This applies to the file itself, not its parents, parent
 * symlinks are always followed.
 */
public enum LinkOption {
    /**
     * Follow symbolic link.
     */
    FOLLOW,

    /**
     * Do not follow symbolic link.
     */
    NOFOLLOW
}
