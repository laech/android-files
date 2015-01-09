package l.files.fs.local;

import com.google.auto.value.AutoValue;

import java.io.File;
import java.net.URI;

import l.files.fs.Path;

@AutoValue
public abstract class LocalPath implements Path {
  LocalPath() {}

  // This is normalized and absolute
  public abstract File toFile();

  /**
   * If the given path is an instance of {@link LocalPath}.
   *
   * @throws IllegalArgumentException if the given path is of an instance of
   *                                  {@link LocalPath}.
   */
  public static LocalPath check(Path path) {
    if (path instanceof LocalPath) {
      return (LocalPath) path;
    }
    throw new IllegalArgumentException(path.uri().toString());
  }

  public static LocalPath of(String path) {
    return of(new File(path));
  }

  public static LocalPath of(File file) {
    return new AutoValue_LocalPath(new File(sanitizedUri(file)));
  }

  @Override public URI uri() {
    return sanitizedUri(toFile());
  }

  private static URI sanitizedUri(File file) {
    /*
     * Don't return File.toURI as it will append a "/" to the end of the URI
     * depending on whether or not the file is a directory, that means two
     * calls to the method before and after the directory is deleted will
     * create two URIs that are not equal.
     */
    URI uri = file.toURI().normalize();
    String uriStr = uri.toString();
    if (!uri.getRawPath().equals("/") && uriStr.endsWith("/")) {
      return URI.create(uriStr.substring(0, uriStr.length() - 1));
    }
    return uri;
  }

  @Override public boolean startsWith(Path that) {
    if (that.parent() == null || that.equals(this)) {
      return true;
    }
    if (that instanceof LocalPath) {
      String thisPath = toFile().getPath();
      String thatPath = ((LocalPath) that).toFile().getPath();
      return thisPath.startsWith(thatPath)
          && thisPath.charAt(thatPath.length()) == '/';
    }
    return false;
  }

  @Override public LocalPath parent() {
    File parent = toFile().getParentFile();
    if (parent == null) {
      return null;
    }
    return of(parent);
  }

  @Override public String name() {
    return toFile().getName();
  }

  @Override public LocalPath resolve(String other) {
    return of(new File(toFile(), other));
  }

  @Override public String toString() {
    return toFile().toString();
  }
}
