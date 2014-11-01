package l.files.fs;

/**
 * A path identifies a resource on a file system. This is a more strict version
 * of the traditional concept of a path.
 * <p/>
 * Implementation requirements for a path:
 * <ul>
 * <li>It is absolute/normalized.</li>
 * <li>The resource does not need to exist on the file system.</li>
 * <li>Every resource has only one path representation, regardless whether or
 * not it exists on the file system. For example, a traditional path for a
 * directory may or may not end with a "/", this is disallowed for
 * implementations of  this interface, as that's two path representations for
 * the directory.</li>
 * </ul>
 */
public interface Path {

  /**
   * Returns true if the given path is an ancestor of this path, or is equal to
   * this path. Will return false if the given path is of a different file
   * system.
   */
  boolean startsWith(Path that);

  /**
   * Returns the parent of this path, or null if this is the root path.
   */
  Path parent();

  /**
   * Returns the name of this path (last path segment),
   * or empty if this is the root path.
   */
  String name();

  /**
   * Returns a new child path with the given name.
   */
  Path child(String name);

  /**
   * Returns the path value. e.g. /dir/a.txt
   */
  @Override String toString();

  /**
   * Two paths are equal if they are from the same file system and have the
   * same path value determined by their file system.
   */
  @Override boolean equals(Object obj);

  @Override int hashCode();
}
