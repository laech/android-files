package l.files.io.file;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import l.files.io.os.ErrnoException;
import l.files.io.os.Stdio;
import l.files.io.os.Unistd;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Integer.parseInt;
import static l.files.io.os.ErrnoException.ENOENT;
import static l.files.io.os.Unistd.F_OK;
import static l.files.io.os.Unistd.access;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getExtension;

public final class Files {

  private static final Pattern NAME_WITH_NUMBER_SUFFIX = Pattern.compile("(.*?\\s+)(\\d+)");

  private Files() {}

  public static void rename(String oldPath, String newPath) throws IOException {
    try {
      Stdio.rename(oldPath, newPath);
    } catch (ErrnoException e) {
      throw new IOException(
          "Failed to rename " + oldPath + " to " + newPath, e);
    }
  }

  public static void remove(String path) throws IOException {
    try {
      Stdio.remove(path);
    } catch (ErrnoException e) {
      throw new IOException("Failed to remove " + path, e);
    }
  }

  /**
   * @param target path of the target file being linked to
   * @param link   path of the link itself
   */
  public static void symlink(String target, String link) throws IOException {
    try {
      Unistd.symlink(target, link);
    } catch (ErrnoException e) {
      throw new IOException(
          "Failed to link target=" + target + ", link=" + link, e);
    }
  }

  public static boolean exists(String path) throws IOException {
    try {
      return access(path, F_OK);
    } catch (ErrnoException e) {
      if (e.errno() == ENOENT) {
        return false;
      }
      throw new IOException("Failed to check file existence " + path, e);
    }
  }

  /**
   * Reads the actual path pointed to by the given symbolic link.
   */
  public static String readlink(String link) throws IOException {
    try {
      return Unistd.readlink(link);
    } catch (ErrnoException e) {
      throw new IOException("Failed to readlink " + link, e);
    }
  }

  /**
   * Returns a normalized path version of the given file.
   * <p/>
   * This resolves the ".", ".." references and returns the absolute file. This
   * is useful when testing whether two files are really equal, for example,
   * {@code new File("./Desktop")} and {@code new File("Desktop")} both
   * references the same file, but {@link File#equals(Object)} will return false
   * because the paths used to construct the two objects are different.
   */
  public static File normalize(File file) {
    return new File(file.toURI().normalize());
  }

  /**
   * Returns a new file with the matched path replaced with a new path.
   * <p/>
   * For example,
   * <pre>
   * File file = new File("/a/b/c");
   * File match = new File("/a/b");
   * File replacement = new File("/d");
   * // replace(file, match, replacement) => new File("/d/c")
   * </pre>
   *
   * @throws IllegalArgumentException if no match found
   * @see #normalize(File)
   */
  public static File replace(File file, File match, File replacement) {
    return doReplace(normalize(file), normalize(match), normalize(replacement));
  }

  private static File doReplace(
      File normalizedFile,
      File normalizedMatch,
      File normalizedReplacement) {
    checkArgument(hierarchy(normalizedFile).contains(normalizedMatch));
    String filePath = normalizedFile.getAbsolutePath();
    String matchPath = normalizedMatch.getAbsolutePath();
    return new File(normalizedReplacement, filePath.substring(matchPath.length()));
  }

  /**
   * Returns true if {@code ancestor} is the file itself or its ancestor.
   *
   * @throws IOException if failed to get the canonical path of the file
   * @see #normalize(File)
   */
  public static boolean isAncestorOrSelf(File file, File ancestor)
      throws IOException {
    Set<File> hierarchy = hierarchy(file.getCanonicalFile());
    File normalized = normalize(ancestor.getCanonicalFile());
    return hierarchy.contains(normalized);
  }

  /**
   * Returns a set containing the file itself and all of its ancestors.
   *
   * @see #normalize(File)
   */
  public static Set<File> hierarchy(File file) {
    checkNotNull(file, "file");
    return hierarchy(normalize(file), new HashSet<File>());
  }

  private static Set<File> hierarchy(File file, Set<File> results) {
    if (file == null) {
      return results;
    }
    results.add(file);
    return hierarchy(file.getParentFile(), results);
  }

  /**
   * Returns a file at {@code dstDir} with the name of {@code source}, if such
   * file exists, append a number at the end of the file name (and before the
   * extension if it's {@link File#isFile()}) until the returned file represents
   * a nonexistent file.
   */
  public static File getNonExistentDestinationFile(File source, File dstDir) {

    String base;
    String last;

    if (source.isDirectory()) {
      base = source.getName();
      last = "";
    } else {
      String name = source.getName();
      base = getBaseName(name);
      last = getExtension(name);
      if (!isNullOrEmpty(last)) {
        last = "." + last;
      }
    }

    File dst;
    while ((dst = new File(dstDir, base + last)).exists()) {
      base = increment(base);
    }

    return dst;
  }

  private static String increment(String base) {
    Matcher matcher = NAME_WITH_NUMBER_SUFFIX.matcher(base);
    if (matcher.matches()) {
      return matcher.group(1) + (parseInt(matcher.group(2)) + 1);
    } else if (base.equals("")) {
      return "2";
    } else {
      return base + " 2";
    }
  }

  /**
   * Calls {@link File#list()} but with option to filter hidden files.
   */
  public static String[] list(File dir, boolean showHiddenFiles) {
    return dir.list(showHiddenFiles ? null : Filter.HIDE_HIDDEN_FILES);
  }

  private static enum Filter implements FilenameFilter {
    HIDE_HIDDEN_FILES {
      @Override public boolean accept(File dir, String filename) {
        return !filename.startsWith(".");
      }
    }
  }
}
