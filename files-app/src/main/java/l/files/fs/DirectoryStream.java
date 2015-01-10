package l.files.fs;

import java.io.Closeable;
import java.util.Iterator;

/**
 * A stream to iterate through the children of a directory, does not follow
 * symbolic links.
 * <p/>
 * {@link #iterator()} can only called once. Each call to the returned
 * iterator {@link Iterator#hasNext() hasNext()} and {@link Iterator#next()
 * next()} will throw an {@link FileSystemException} if an error was
 * encountered while reading the next child.
 * <p/>
 * An instance must be closed when no longer needed.
 */
public interface DirectoryStream extends Iterable<PathEntry>, Closeable {

  /**
   * @throws FileSystemException if failed to close this stream
   */
  @Override void close();
}
