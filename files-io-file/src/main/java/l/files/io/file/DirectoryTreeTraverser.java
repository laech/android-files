package l.files.io.file;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.TreeTraverser;

import java.io.IOException;

import l.files.logging.Logger;

import static java.util.Collections.emptyList;
import static org.apache.commons.io.FilenameUtils.concat;

/**
 * Traverses a directory and returns all the child paths.
 */
public final class DirectoryTreeTraverser extends TreeTraverser<String> {

  private static final Logger logger = Logger.get(DirectoryTreeTraverser.class);

  private static final DirectoryTreeTraverser instance =
      new DirectoryTreeTraverser();

  private DirectoryTreeTraverser() {}

  public static DirectoryTreeTraverser get() {
    return instance;
  }

  @Override public Iterable<String> children(final String root) {
    DirectoryStream stream = null;
    try {

      stream = DirectoryStream.open(root);
      return children(root, stream);

    } catch (IOException e) {
      logger.warn(e);
      return emptyList();

    } finally {
      close(stream);
    }
  }

  private Iterable<String> children(String parent, DirectoryStream stream) {
    ImmutableList.Builder<String> builder = ImmutableList.builder();
    for (DirectoryStream.Entry child : stream) {
      builder.add(concat(parent, child.name()));
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
}
