package l.files.common.io;

import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.util.Collections.unmodifiableSet;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

public final class Files {
  private Files() {}

  public static File[] toFiles(String... paths) {
    File[] files = new File[paths.length];
    for (int i = 0; i < files.length; ++i) {
      files[i] = new File(paths[i]);
    }
    return files;
  }

  public static String[] toAbsolutePaths(File... files) {
    String[] paths = new String[files.length];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = files[i].getAbsolutePath();
    }
    return paths;
  }

  public static Set<String> toAbsolutePaths(Set<File> files) {
    Set<String> paths = newHashSetWithExpectedSize(files.size());
    for (File file : files) {
      paths.add(file.getAbsolutePath());
    }
    return unmodifiableSet(paths);
  }

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
