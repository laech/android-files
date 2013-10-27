package l.files.app.io;

import android.os.AsyncTask;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.TreeTraverser;
import l.files.common.base.ValueObject;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.io.FileUtils.isSymlink;

/**
 * When some source files/directories are to be pasted into a destination, an
 * inspection could be perform prior to the actual operation to find potential
 * problems, so that these problems may be presented and resolved before the
 * actual operation takes place.
 */
class PasteInspection
    extends AsyncTask<Void, PasteInspection.Progress, PasteInspection.Result> {

  private static final long PROGRESS_DELAY_MILLIS = 1000;

  private final int id;
  private final Set<File> sources;
  private final File destDir;
  private final Type type;

  /**
   * @param id the ID to assign to this inspection
   * @param sources the inspection target, sources to be pasted
   * @param destDir the destination directory for the sources to be pasted into
   * @param type the type of inspection
   */
  PasteInspection(int id, Set<File> sources, File destDir, Type type) {
    this.id = id;
    this.sources = ImmutableSet.copyOf(checkNotNull(sources, "sources"));
    this.destDir = checkNotNull(destDir, "destDir");
    this.type = checkNotNull(type, "type");
  }

  public int id() {
    return id;
  }

  protected Set<File> sources() {
    return sources;
  }

  protected File destination() {
    return destDir;
  }

  protected Type type() {
    return type;
  }

  @Override protected Result doInBackground(Void... params) {
    Map<File, File> conflicts = newHashMap();
    Set<File> errors = newHashSet();
    int count = 0;
    long start = currentTimeMillis();

    for (File src : sources) {
      Traverser traverser = new Traverser(src.getParentFile(), destDir);

      for (File file : traverser.breadthFirstTraversal(src)) {
        if (isCancelled()) return null;
        if (file.isFile()) count++;

        long now = currentTimeMillis();
        if (now - start >= PROGRESS_DELAY_MILLIS) {
          start = now;
          publishProgress(new Progress(count));
        }
      }

      conflicts.putAll(traverser.conflicts);
      errors.addAll(traverser.errors);
    }

    return new Result(conflicts, errors);
  }

  public static enum Type {
    MOVE, COPY
  }

  public static final class Result extends ValueObject {
    private final Map<File, File> conflicts;
    private final Set<File> errors;

    public Result(Map<File, File> conflicts, Set<File> errors) {
      this.conflicts = ImmutableMap.copyOf(checkNotNull(conflicts, "conflicts"));
      this.errors = ImmutableSet.copyOf(checkNotNull(errors, "errors"));
    }

    /**
     * Returns the conflicts found.
     * <p/>
     * The keys of the returned map are source files/directories, the values are
     * the corresponding destination files/directories of the sources.
     * <p/>
     * There is a number of reasons for a conflict, for example, a file with the
     * same name as the source already exists at the destination; or a source is
     * a directory but the destination is a file.
     */
    public Map<File, File> conflicts() {
      return conflicts;
    }

    /**
     * Returns the files failed to be inspected, such as restricted
     * directories.
     */
    public Set<File> errors() {
      return errors;
    }
  }

  public static final class Progress extends ValueObject {
    private final int count;

    public Progress(int count) {
      this.count = count;
    }

    /**
     * Returns the count of source files inspected, excluding directories.
     */
    public int count() {
      return count;
    }
  }

  private static final class Traverser extends TreeTraverser<File> {
    final Map<File, File> conflicts = newHashMap();
    final Set<File> errors = newHashSet();
    final File srcDir;
    final File destDir;

    Traverser(File srcDir, File destDir) {
      this.srcDir = srcDir;
      this.destDir = destDir;
    }

    @Override public Iterable<File> children(File root) {
      try {
        if (isSymlink(root)) return emptyList();
      } catch (IOException e) {
        errors.add(root);
      }
      checkConflict(root);
      return list(root);
    }

    private Iterable<File> list(File file) {
      if (!file.isDirectory()) return emptyList();

      File[] children = file.listFiles();
      if (children == null) {
        errors.add(file);
        return emptyList();
      }
      return asList(children);
    }

    private void checkConflict(File src) {
      String srcPath = src.getAbsolutePath();
      String srcDirPath = srcDir.getAbsolutePath();
      File dest = new File(destDir, srcPath.replace(srcDirPath, ""));
      boolean bothExist = src.exists() && dest.exists();
      boolean sameType = src.isDirectory() && dest.isDirectory();
      if (bothExist && !sameType) conflicts.put(src, dest);
    }
  }
}
