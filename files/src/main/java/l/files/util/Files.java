package l.files.util;

import java.io.File;

import static l.files.util.FileFilters.HIDE_HIDDEN_FILES;

public final class Files {

  /**
   * Gets the file extension of the file, or "" if no extension.
   */
  public static String getFileExtension(File file) {
    String name = file.getName();
    int dotIndex = name.lastIndexOf('.');
    return dotIndex == -1 ? "" : name.substring(dotIndex + 1);
  }

  public static File[] listFiles(File directory, boolean showHiddenFiles) {
    return directory.listFiles(showHiddenFiles ? null : HIDE_HIDDEN_FILES);
  }

  private Files() {
  }
}
