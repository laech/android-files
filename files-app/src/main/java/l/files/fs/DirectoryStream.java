package l.files.fs;

import java.io.Closeable;
import java.util.Iterator;

/**
 * A stream to iterate through the children of a directory.
 * <p/>
 * {@link #iterator()} can only called once. Each call to the returned
 * iterator {@link Iterator#hasNext() hasNext()} and {@link Iterator#next()
 * next()} will throw an {@link DirectoryIteratorException} if an error was
 * encountered while reading the next child.
 * <p/>
 * An instance must be closed when no longer needed.
 */
public interface DirectoryStream<T extends DirectoryStream.Entry>
    extends Iterable<T>, Closeable {

  static interface Entry {
    FileId file();
  }
}
