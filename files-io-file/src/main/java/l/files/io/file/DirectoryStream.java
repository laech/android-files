package l.files.io.file;

import com.google.auto.value.AutoValue;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import l.files.io.os.Dirent;
import l.files.io.os.ErrnoException;

import static l.files.io.os.Dirent.closedir;
import static l.files.io.os.Dirent.opendir;
import static l.files.io.os.Dirent.readdir;

/**
 * A stream to iterate through the children of a directory.
 *
 * <p>{@link #iterator()} can only called once. Each call to the returned
 * iterator {@link Iterator#hasNext() hasNext()} and {@link Iterator#next()
 * next()} will throw an {@link DirectoryIteratorException} if an error was
 * encountered while reading the next child.
 *
 * <p>An instance must be closed when no longer needed.
 */
public final class DirectoryStream
    implements Iterable<DirectoryStream.Entry>, Closeable {

  /*
   * Design note: this basically uses <dirent.h> to read directory entries,
   * returning simple DirectoryStream.Entry without using stat/lstat will yield
   * much better performance when directory is large.
   */

  private final long dir;
  private boolean iterated;

  private DirectoryStream(long dir) {
    this.dir = dir;
  }

  /**
   * @throws IOException includes path is not accessible or doesn't exist
   */
  public static DirectoryStream open(String path) throws IOException {
    try {
      long dir = opendir(path);
      return new DirectoryStream(dir);
    } catch (ErrnoException e) {
      throw new IOException("Failed to open " + path, e);
    }
  }

  @Override public void close() throws IOException {
    closedir(dir);
  }

  @Override public Iterator<Entry> iterator() {
    if (iterated) {
      throw new IllegalStateException("iterator() has already been called");
    }
    iterated = true;
    return new DirectoryIterator(dir);
  }

  private static final class DirectoryIterator implements Iterator<Entry> {
    private final long dir;
    private Entry next;

    DirectoryIterator(long dir) {
      this.dir = dir;
    }

    @Override public boolean hasNext() {
      if (next == null) {
        next = readNext();
      }
      return next != null;
    }

    @Override public Entry next() {
      Entry entry = next;
      if (entry != null) {
        next = null;
      } else {
        entry = readNext();
      }
      if (entry == null) {
        throw new NoSuchElementException();
      }
      return entry;
    }

    private Entry readNext() {
      try {

        Dirent entry;
        do {
          entry = readdir(dir);
        } while (entry != null && isSelfOrParent(entry));

        if (entry != null) {
          return Entry.create(entry);
        } else {
          return null;
        }

      } catch (ErrnoException e) {
        throw new DirectoryIteratorException(e);
      }
    }

    private boolean isSelfOrParent(Dirent entry) {
      return entry.name().equals(".") || entry.name().equals("..");
    }

    @Override public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  @AutoValue
  public static abstract class Entry {

    public static final int TYPE_UNKNOWN = Dirent.DT_UNKNOWN;
    public static final int TYPE_FIFO = Dirent.DT_FIFO;
    public static final int TYPE_CHR = Dirent.DT_CHR;
    public static final int TYPE_DIR = Dirent.DT_DIR;
    public static final int TYPE_BLK = Dirent.DT_BLK;
    public static final int TYPE_REG = Dirent.DT_REG;
    public static final int TYPE_LNK = Dirent.DT_LNK;
    public static final int TYPE_SOCK = Dirent.DT_SOCK;
    public static final int TYPE_WHT = Dirent.DT_WHT;

    Entry() {}

    public abstract long ino();

    public abstract String name();

    public abstract int type();

    static Entry create(Dirent entry) {
      return create(entry.ino(), entry.name(), entry.type());
    }

    public static Entry create(long ino, String name, int type) {
      return new AutoValue_DirectoryStream_Entry(ino, name, type);
    }
  }
}
