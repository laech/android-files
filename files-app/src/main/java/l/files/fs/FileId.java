package l.files.fs;

import com.google.auto.value.AutoValue;

import java.io.File;
import java.net.URI;

/**
 * The universally unique identifier of a file.
 */
@AutoValue
public abstract class FileId {
  FileId() {}

  abstract String id();

  /**
   * Create a ID for the given path.
   */
  public static FileId of(File file) {
    return new AutoValue_FileId(toId(file));
  }

  private static String toId(File file) {
    /*
     * Don't return File.toURI as it will append a "/" to the end of the URI
     * depending on whether or not the file is a directory, that means two calls
     * to the method before and after the directory is deleted will create two
     * URIs that are not equal.
     */
    URI uri = file.toURI().normalize();
    String uriStr = uri.toString();
    if (!uri.getRawPath().equals("/") && uriStr.endsWith("/")) {
      return uriStr.substring(0, uriStr.length() - 1);
    }
    return uriStr;
  }

  @Override public String toString() {
    return id();
  }

  @Override public int hashCode() {
    return id().hashCode();
  }
}
