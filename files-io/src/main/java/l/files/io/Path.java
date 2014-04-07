package l.files.io;

import com.google.auto.value.AutoValue;

import java.io.File;

import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;

/**
 * Represents a normalized local file system path, such as {@code
 * /sdcard/hello.txt}.
 */
@AutoValue
public abstract class Path {

  public static final Path ROOT = new AutoValue_Path("/");

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

  @Override public String toString() {
    return path();
  }
}
