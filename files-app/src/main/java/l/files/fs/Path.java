package l.files.fs;

import java.net.URI;

/**
 * Identifies a resource on a file system.
 * With the following requirements:
 * <ul>
 * <li>A path and its {@link #uri()} are always absolute and normalized.</li>
 * <li>The resource does not need to exist on the file system.</li>
 * <li>Every resource has only one path and one {@link #uri()} representation,
 * regardless whether or not it exists on the file system.
 * For example, a traditional file URI for a directory may or may not end with
 * a "/" depending on its existence, this is disallowed for implementations of
 * this interface, as that's two representations for the resource.</li>
 * </ul>
 */
public interface Path {

  /**
   * The file system this path belongs to.
   */
  FileSystem system();

  /**
   * The absolute/normalized URI representation of this path.
   */
  URI uri();

  /**
   * Gets the parent path, returns null if this is the root path.
   */
  Path parent();

  /**
   * Gets the name part of this path, or empty if this is the root path
   */
  String name();

  /**
   * Resolves the given path/name relative to this path.
   */
  Path resolve(String other);

  /**
   * Returns true if the this path starts with the given path, including if
   * the two paths are the same.
   */
  boolean startsWith(Path path);

  /**
   * Returns the path string, such as {@code /a/b.txt}
   */
  @Override String toString();

  /**
   * Returns true if the two path strings are the same.
   */
  @Override boolean equals(Object obj);

  @Override int hashCode();
}
