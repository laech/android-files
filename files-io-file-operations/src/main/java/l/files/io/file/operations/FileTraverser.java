package l.files.io.file.operations;

import com.google.common.collect.TreeTraverser;

import java.io.IOException;
import java.util.List;

import l.files.io.file.DirectoryStream;
import l.files.io.file.FileInfo;
import l.files.logging.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.emptyList;
import static l.files.io.file.DirectoryStream.Entry;

final class FileTraverser extends TreeTraverser<FileInfo> {

  // TODO provide a version that returns directory entry without stat for efficient, (Delete etc)

  private static final Logger logger = Logger.get(FileTraverser.class);

  private static final FileTraverser instance = new FileTraverser();

  public static FileTraverser get() {
    return instance;
  }

  @Override public Iterable<FileInfo> children(FileInfo parent) {
    if (!parent.isDirectory()) {
      return emptyList();
    }

    DirectoryStream stream = null;
    try {
      stream = DirectoryStream.open(parent.path());
      return children(parent, stream);
    } catch (IOException e) {
      // No longer exists or not accessible, skip
      logger.warn(e);
      return emptyList();
    } finally {
      close(stream);
    }
  }

  private List<FileInfo> children(FileInfo parent, DirectoryStream stream) {
    List<FileInfo> children = newArrayList();
    for (Entry entry : stream) {
      try {
        children.add(FileInfo.get(parent.path(), entry.name()));
      } catch (IOException e) {
        // No longer exists or not accessible, skip
        logger.warn(e);
      }
    }
    return children;
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
}
