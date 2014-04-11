package l.files.io;

import com.google.auto.value.AutoValue;

import java.io.File;

import static org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;

/**
 * Represents a normalized local file system path, such as {@code
 * /sdcard/hello.txt}.
 */
@AutoValue
public abstract class Path {

  public static final Path ROOT = new AutoValue_Path("/");

  private Path parent;
  private String name;

  Path() {}

  abstract String path();

  /**
   * Returns true if the given path is an ancestor of this path.
   */
  public boolean startsWith(Path that) {
    if (that.equals(ROOT) || that.equals(this)) {
      return true;
    }
    String thisPath = path();
    String thatPath = that.path();
    return thisPath.startsWith(thatPath)
        && thisPath.charAt(thatPath.length()) == '/';
  }

  public static Path from(File file) {
    String normalizedPath = normalizeNoEndSeparator(file.getAbsolutePath());
    return new AutoValue_Path(normalizedPath);
  }

  public static Path from(String path) {
    return from(new File(path));
  }

  public File toFile() {
    return new File(path());
  }

  /**
   * Returns the parent of this path, or null if this is the root path.
   */
  public Path parent() {
    if (ROOT.equals(this)) {
      return null;
    }
    return parent != null ? parent
        : (parent = from(getFullPathNoEndSeparator(path())));
  }

  /**
   * Returns the name of this path, or empty if this is the root path.
   */
  public String name() {
    return name != null ? name : (name = getName(path()));
  }

  /**
   * Returns a new child path with the given name.
   */
  public Path child(String name) {
    return from(new File(path(), name));
  }

  /**
   * Returns the path represented by this instance.
   */
  @Override public String toString() {
    return path();
  }
}
