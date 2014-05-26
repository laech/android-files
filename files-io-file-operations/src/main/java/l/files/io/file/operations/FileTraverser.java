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

  private static final Logger logger = Logger.get(FileTraverser.class);

  private static final FileTraverser instance = new FileTraverser();

  public static FileTraverser get() {
    return instance;
  }

  @Override public Iterable<FileInfo> children(FileInfo parent) {
    if (!parent.isDirectory()) {
      return emptyList();
    }

    DirectoryStream stream;
    try {
      stream = DirectoryStream.open(parent.path());
    } catch (IOException e) {
      // No longer exists or not accessible, skip
      logger.warn(e);
      return emptyList();
    }
    return children(parent, stream);
  }

  private List<FileInfo> children(FileInfo parent, DirectoryStream stream) {
    List<FileInfo> children = newArrayList();
    try {
      for (Entry entry : stream) {
        try {
          children.add(FileInfo.get(parent.path(), entry.name()));
        } catch (IOException e) {
          // No longer exists or not accessible, skip
          logger.warn(e);
        }
      }
    } finally {
      close(stream);
    }
    return children;
  }

  private static void close(DirectoryStream stream) {
    try {
      stream.close();
    } catch (IOException e) {
      logger.warn(e);
    }
  }
}
