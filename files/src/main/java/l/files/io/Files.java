package l.files.io;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.io.File;
import java.io.FilenameFilter;

public final class Files {

  private static final FilenameFilter HIDE_HIDDEN_FILES = new FilenameFilter() {
    @Override public boolean accept(File dir, String filename) {
      return !filename.startsWith(".");
    }
  };

  private Files() {}

  public static Function<File, String> name() {
    return FileNameFunction.INSTANCE;
  }

  public static Predicate<File> canRead() {
    return FilePredicate.CAN_READ;
  }

  public static Predicate<File> exists() {
    return FilePredicate.EXISTS;
  }

  public static File[] listFiles(File directory, boolean showHiddenFiles) {
    return directory.listFiles(showHiddenFiles ? null : HIDE_HIDDEN_FILES);
  }

}
