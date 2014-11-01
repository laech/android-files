package l.files.fs.local;

import com.google.auto.value.AutoValue;

import java.io.File;

import l.files.fs.FileId;
import l.files.fs.Path;

import static org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.normalizeNoEndSeparator;

@AutoValue
public abstract class LocalPath implements Path {

  public static final LocalPath ROOT = new AutoValue_LocalPath("/");

  private LocalPath parent;
  private String name;

  LocalPath() {}

  abstract String path();

  @Override public boolean startsWith(Path path) {
    if (!(path instanceof LocalPath)) {
      return false;
    }
    LocalPath that = (LocalPath) path;
    if (that.equals(ROOT) || that.equals(this)) {
      return true;
    }
    String thisPath = path();
    String thatPath = that.path();
    return thisPath.startsWith(thatPath)
        && thisPath.charAt(thatPath.length()) == '/';
  }

  public static LocalPath from(File file) {
    String normalizedPath = normalizeNoEndSeparator(file.getAbsolutePath());
    return new AutoValue_LocalPath(normalizedPath);
  }

  public static LocalPath from(String path) {
    return from(new File(path));
  }

  static LocalPath of(FileId file) {
    return from(new File(file.toUri()));
  }

  @Override public LocalPath parent() {
    if (ROOT.equals(this)) {
      return null;
    }
    return parent != null ? parent
        : (parent = from(getFullPathNoEndSeparator(path())));
  }

  @Override public String name() {
    return name != null ? name : (name = getName(path()));
  }

  @Override public LocalPath child(String name) {
    return from(new File(path(), name));
  }

  @Override public String toString() {
    return path();
  }

  /**
   * @throws IllegalArgumentException if the given path is not of this type
   */
  static void checkPath(Path path) {
    if (!(path instanceof LocalPath)) {
      throw new IllegalArgumentException(path.getClass() + ": " + path);
    }
  }
}
