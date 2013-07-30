package l.files.common.io;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.io.File;
import java.io.FilenameFilter;

public final class Files {
  private Files() {}

  /**
   * Returns a function for {@link File#getName()}.
   */
  public static Function<File, String> name() {
    return FileNameFunction.INSTANCE;
  }

  /**
   * Returns a function for {@link File#canRead()}.
   */
  public static Predicate<File> canRead() {
    return FilePredicate.CAN_READ;
  }

  /**
   * Calls {@link File#listFiles()} but with option to filter hidden files.
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
