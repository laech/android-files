package l.files.common.io;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Collection;

import static java.lang.String.format;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;

public final class Files {
  private Files() {}

  /**
   * Compares {@code target}'s canonical path to {@code prefix}'s canonical path
   * to determine if {@code prefix} is a ancestor of {@code target}.
   *
   * @see File#getCanonicalPath()
   * @see File#getCanonicalFile()
   */
  public static boolean canonicalStartsWith(File target, File prefix)
      throws IOException {
    // TODO
    String targetPath = target.getCanonicalPath();
    String prefixPath = prefix.getCanonicalPath();
    return targetPath.equals(prefixPath) ||
        targetPath.startsWith(prefixPath + "/");
  }

  /**
   * Returns a file at {@code destDir} with the name of {@code source}, if such
   * file exists, append a number at the end of the file name (and before the
   * extension if it's {@link java.io.File#isFile()}) until the returned file
   * represents a nonexistent file.
   */
  public static File getNonExistentDestinationFile(File source, File destDir) {
    String fullName = source.getName();
    String baseName = getBaseName(fullName);
    String extension = getExtension(fullName);
    File file = new File(destDir, source.getName());
    for (int i = 2; file.exists(); ++i) {
      String newName = source.isFile() && !"".equals(baseName) && !"".equals(extension)
          ? format("%s %d.%s", baseName, i, extension)
          : format("%s %d", fullName, i);
      file = new File(destDir, newName);
    }
    return file;
  }

  public static File[] toFiles(Collection<String> paths) {
    return toFiles(paths.toArray(new String[paths.size()]));
  }

  public static File[] toFiles(String... absolutePaths) {
    File[] files = new File[absolutePaths.length];
    for (int i = 0; i < files.length; ++i) {
      files[i] = new File(absolutePaths[i]);
    }
    return files;
  }

  public static String[] toAbsolutePaths(Collection<File> files) {
    return toAbsolutePaths(files.toArray(new File[files.size()]));
  }

  public static String[] toAbsolutePaths(File... files) {
    String[] paths = new String[files.length];
    for (int i = 0; i < paths.length; ++i) {
      paths[i] = files[i].getAbsolutePath();
    }
    return paths;
  }

  /**
   * Returns a function for {@link File#getName()}.
   */
  public static Function<File, String> name() {
    return FileNameFunction.INSTANCE;
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
