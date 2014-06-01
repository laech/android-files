package l.files.io.file;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeTraverser;

import java.io.File;
import java.io.IOException;

import l.files.logging.Logger;

import static java.util.Collections.emptyList;
import static l.files.io.file.DirectoryStream.Entry.TYPE_DIR;
import static org.apache.commons.io.FilenameUtils.concat;

/**
 * Traverses a directory and returns all the child paths.
 */
public final class DirectoryTreeTraverser
    extends TreeTraverser<DirectoryTreeTraverser.Entry> {

  /*
   * Design note: traverses a directory tree, return a minimal entry structure
   * without additional file information (by calling stat/lstat) to ensure the
   * traversal is fast on large directories. Callers can then get file
   * information during traversal as needed outside of this class.
   */

  private static final Logger logger = Logger.get(DirectoryTreeTraverser.class);

  private static final DirectoryTreeTraverser instance =
      new DirectoryTreeTraverser();

  private DirectoryTreeTraverser() {}

  public static DirectoryTreeTraverser get() {
    return instance;
  }

  @Override public Iterable<Entry> children(final Entry root) {
    if (!root.isDirectory()) {
      return emptyList();
    }

    DirectoryStream stream = null;
    try {

      stream = DirectoryStream.open(root.path());
      return children(root.path(), stream);

    } catch (IOException e) {
      logger.warn(e);
      return emptyList();

    } finally {
      close(stream);
    }
  }

  private Iterable<Entry> children(String parent, DirectoryStream stream) {
    ImmutableList.Builder<Entry> builder = ImmutableList.builder();
    for (DirectoryStream.Entry child : stream) {
      // Ensure not using stat/lstat to get entry type, see design note at top
      boolean isDirectory = child.type() == TYPE_DIR;
      String path = concat(parent, child.name());
      builder.add(Entry.create(path, isDirectory));
    }
    return builder.build();
  }

  private static void close(DirectoryStream stream) {
    if (stream == null) {
      return;
    }
    try {
      stream.close();
    } catch (IOException e) {
      logger.error(e);
    }
  }

  @AutoValue
  public static abstract class Entry {
    Entry() {}

    public abstract String path();
    public abstract boolean isDirectory();

    public static Entry create(String path) {
      // File.isDirectory will call stat, do not use this during traversal
      return create(path, new File(path).isDirectory());
    }

    static Entry create(String path, boolean isDirectory) {
      return new AutoValue_DirectoryTreeTraverser_Entry(path, isDirectory);
    }
  }
}
