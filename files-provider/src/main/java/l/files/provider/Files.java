package l.files.provider;

import java.io.File;
import java.io.FilenameFilter;

public class Files {
  private Files() {}

  /**
   * Calls {@link java.io.File#listFiles()} but with option to filter hidden
   * files.
   */
  public static File[] listFiles(File dir, boolean showHiddenFiles) {
    return dir.listFiles(showHiddenFiles ? null : Filter.HIDE_HIDDEN_FILES);
  }

  private static enum Filter implements FilenameFilter {
    HIDE_HIDDEN_FILES {
      @Override public boolean accept(File dir, String filename) {
        return !filename.startsWith(".");
      }
    }
  }
}
