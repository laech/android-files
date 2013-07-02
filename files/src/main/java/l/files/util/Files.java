package l.files.util;

import java.io.File;

import static l.files.util.FileFilters.HIDE_HIDDEN_FILES;

public final class Files {

  public static File[] listFiles(File directory, boolean showHiddenFiles) {
    return directory.listFiles(showHiddenFiles ? null : HIDE_HIDDEN_FILES);
  }

  private Files() {
  }
}
