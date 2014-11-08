package l.files.fs.local;

import com.google.auto.value.AutoValue;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.io.File;

import l.files.fs.DirectoryEntry;
import l.files.fs.DirectoryTreeTraverser;
import l.files.fs.Path;
import l.files.fs.FileSystemException;
import l.files.logging.Logger;

import static java.util.Collections.emptyList;
import static l.files.fs.local.LocalDirectoryStream.Entry.TYPE_DIR;

public final class LocalDirectoryTreeTraverser
    extends DirectoryTreeTraverser<LocalDirectoryTreeTraverser.Entry> {

  /*
   * Design note: traverses a directory tree, return a minimal entry structure
   * without additional file information (by calling stat/lstat) to ensure the
   * traversal is fast on large directories. Callers can then get file
   * information during traversal as needed outside of this class.
   */

  private static final Logger logger = Logger.get(LocalDirectoryTreeTraverser.class);

  private static final LocalDirectoryTreeTraverser instance =
      new LocalDirectoryTreeTraverser();

  private LocalDirectoryTreeTraverser() {}

  public static LocalDirectoryTreeTraverser get() {
    return instance;
  }

  @Override public Iterable<Entry> children(Entry root) {
    if (!root.isDirectory()) {
      return emptyList();
    }

    try (LocalDirectoryStream stream = LocalDirectoryStream.open(root.path())) {
      return children(stream);
    } catch (FileSystemException e) {
      logger.warn(e);
      return emptyList();
    }
  }

  private Iterable<Entry> children(LocalDirectoryStream stream) {
    ImmutableList.Builder<Entry> builder = ImmutableList.builder();
    for (LocalDirectoryStream.Entry child : stream) {
      // Ensure not using stat/lstat to get entry type, see design note at top
      boolean isDirectory = child.type() == TYPE_DIR;
      builder.add(Entry.create(child.path(), isDirectory));
    }
    return builder.build();
  }

  @Override public FluentIterable<Entry> preOrderTraversal(Path root) {
    return preOrderTraversal(Entry.stat(root));
  }

  @Override public FluentIterable<Entry> postOrderTraversal(Path root) {
    return postOrderTraversal(Entry.stat(root));
  }

  @Override public FluentIterable<Entry> breadthFirstTraversal(Path root) {
    return breadthFirstTraversal(Entry.stat(root));
  }

  @AutoValue
  public static abstract class Entry implements DirectoryEntry {
    Entry() {}

    @Override public abstract Path path();

    abstract boolean isDirectory();

    /**
     * @throws FileSystemException if failed to get file status
     */
    static Entry stat(File file) {
      return stat(LocalPath.of(file));
    }

    /**
     * @throws FileSystemException if failed to get file status
     */
    static Entry stat(Path path) {
      LocalFileStatus status = LocalFileStatus.stat(path, false);
      return new AutoValue_LocalDirectoryTreeTraverser_Entry(path, status.isDirectory());
    }

    @Deprecated
    public static Entry create(String path) {
      // File.isDirectory will call stat, do not use this during traversal
      File file = new File(path);
      return create(LocalPath.of(file), file.isDirectory());
    }

    static Entry create(Path path, boolean isDir) {
      return new AutoValue_LocalDirectoryTreeTraverser_Entry(path, isDir);
    }
  }
}
