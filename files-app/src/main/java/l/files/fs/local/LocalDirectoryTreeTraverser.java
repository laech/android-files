package l.files.fs.local;

import com.google.auto.value.AutoValue;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import java.io.File;

import l.files.fs.DirectoryEntry;
import l.files.fs.DirectoryTreeTraverser;
import l.files.fs.FileId;
import l.files.fs.FileSystemException;
import l.files.logging.Logger;

import static java.util.Collections.emptyList;
import static l.files.fs.local.LocalDirectoryStream.Entry.TYPE_DIR;
import static org.apache.commons.io.FilenameUtils.concat;

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
      return children(root.path(), stream);
    } catch (FileSystemException e) {
      logger.warn(e);
      return emptyList();
    }
  }

  private Iterable<Entry> children(String parent, LocalDirectoryStream stream) {
    ImmutableList.Builder<Entry> builder = ImmutableList.builder();
    for (LocalDirectoryStream.Entry child : stream) {
      // Ensure not using stat/lstat to get entry type, see design note at top
      boolean isDirectory = child.type() == TYPE_DIR;
      String path = concat(parent, child.name());
      builder.add(Entry.create(path, isDirectory));
    }
    return builder.build();
  }

  @Override
  public FluentIterable<Entry> preOrderTraversal(FileId root) {
    return preOrderTraversal(Entry.create(new File(root.toUri())));
  }

  @Override
  public FluentIterable<Entry> postOrderTraversal(FileId root) {
    return postOrderTraversal(Entry.create(new File(root.toUri())));
  }

  @Override
  public FluentIterable<Entry> breadthFirstTraversal(FileId root) {
    return breadthFirstTraversal(Entry.create(new File(root.toUri())));
  }

  @AutoValue
  public static abstract class Entry implements DirectoryEntry {
    Entry() {}

    @Override public abstract FileId file();

    public abstract String path();
    public abstract boolean isDirectory();

    static Entry create(File file) {
      return create(file.getPath(), file.isDirectory());
    }

    public static Entry create(String path) {
      // File.isDirectory will call stat, do not use this during traversal
      return create(path, new File(path).isDirectory());
    }

    static Entry create(String path, boolean isDirectory) {
      FileId id = FileId.of(new File(path));
      return new AutoValue_LocalDirectoryTreeTraverser_Entry(id, path, isDirectory);
    }
  }
}
