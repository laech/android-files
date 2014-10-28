package l.files.fs.local;

import com.google.auto.value.AutoValue;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import l.files.fs.DirectoryIteratorException;
import l.files.fs.DirectoryStream;
import l.files.fs.FileId;
import l.files.fs.FileSystemException;

import static com.google.common.base.Preconditions.checkNotNull;

final class LocalDirectoryStream
    implements DirectoryStream<LocalDirectoryStream.LocalEntry> {

  /*
   * Design note: this basically uses <dirent.h> to read directory entries,
   * returning simple DirectoryStream.Entry without using stat/lstat will yield
   * much better performance when directory is large.
   */

  private final File parent;
  private final long dir;
  private boolean iterated;

  private LocalDirectoryStream(File parent, long dir) {
    this.parent = parent;
    this.dir = dir;
  }

  /**
   * Opens a new stream for the given directory.
   *
   * @throws FileSystemException if failed to open directory
   */
  static LocalDirectoryStream open(File directory) {
    checkNotNull(directory);
    try {
      long dir = Dirent.opendir(directory.getPath());
      return new LocalDirectoryStream(directory, dir);
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
  }

  /**
   * Opens a new stream for the given directory.
   *
   * @throws FileSystemException if failed to open directory
   */
  static LocalDirectoryStream open(String path) {
    return open(new File(path));
  }

  @Override public void close() throws IOException {
    Dirent.closedir(dir);
  }

  @Override
  @SuppressWarnings("unchecked")
  public Iterator<LocalEntry> iterator() {
    if (iterated) {
      throw new IllegalStateException("iterator() has already been called");
    }
    iterated = true;
    return new DirectoryIterator(parent, dir);
  }

  private static final class DirectoryIterator implements Iterator<LocalEntry> {
    private final File parent;
    private final long dir;
    private LocalEntry next;

    DirectoryIterator(File parent, long dir) {
      this.parent = parent;
      this.dir = dir;
    }

    @Override public boolean hasNext() {
      if (next == null) {
        next = readNext();
      }
      return next != null;
    }

    @Override public LocalEntry next() {
      LocalEntry entry = next;
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

    private LocalEntry readNext() {
      try {

        Dirent entry;
        do {
          entry = Dirent.readdir(dir);
        } while (entry != null && isSelfOrParent(entry));

        if (entry != null) {
          return LocalEntry.create(parent, entry);
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
  @SuppressWarnings("UnusedDeclaration")
  static abstract class LocalEntry implements DirectoryStream.Entry {

    static final int TYPE_UNKNOWN = Dirent.DT_UNKNOWN;
    static final int TYPE_FIFO = Dirent.DT_FIFO;
    static final int TYPE_CHR = Dirent.DT_CHR;
    static final int TYPE_DIR = Dirent.DT_DIR;
    static final int TYPE_BLK = Dirent.DT_BLK;
    static final int TYPE_REG = Dirent.DT_REG;
    static final int TYPE_LNK = Dirent.DT_LNK;
    static final int TYPE_SOCK = Dirent.DT_SOCK;
    static final int TYPE_WHT = Dirent.DT_WHT;

    LocalEntry() {}

    @Override public abstract FileId file();

    abstract long ino();

    abstract String name();

    abstract int type();

    static LocalEntry create(File parent, Dirent entry) {
      return create(parent, entry.ino(), entry.name(), entry.type());
    }

    static LocalEntry create(File parent, long ino, String name, int type) {
      FileId id = FileId.of(new File(parent, name));
      return new AutoValue_LocalDirectoryStream_LocalEntry(id, ino, name, type);
    }
  }
}
