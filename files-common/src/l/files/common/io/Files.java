package l.files.common.io;

import static com.google.common.collect.Sets.newHashSetWithExpectedSize;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

public final class Files {
  private Files() {}

  /**
   * Returns a file with the following properties:
   * <ul>
   *   <li>It does not exist.</li>
   *   <li>Its parent directory is {@code destinationDirectory}.</li>
   *   <li>
   *     Its name is the name of {@code source} if {@code destinationDirectory}
   *     does not have a file/directory with the same name.
   *   </li>
   *   <li>
   *     If {@code destinationDirectory} already have a file with same name, the
   *     returned file's name will be based on the name of {@code source} with
   *     extra characters added renamed so that it will not conflict an existing
   *     file at {@code destinationDirectory}.
   *   </li>
   * </ul>
   */
  public static File getNonExistentDestinationFile(File source, File destinationDirectory) {
    String fullName = source.getName();
    String baseName = getBaseName(fullName);
    String extension = getExtension(fullName);
    File file = new File(destinationDirectory, source.getName());
    for (int i = 2; file.exists(); ++i) {
      String newName = source.isFile() && !"".equals(baseName) && !"".equals(extension)
          ? format("%s %d.%s", baseName, i, extension)
          : format("%s %d", fullName, i);
      file = new File(destinationDirectory, newName);
    }
    return file;
  }

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
