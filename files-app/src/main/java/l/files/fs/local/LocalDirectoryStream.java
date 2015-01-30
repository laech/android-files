package l.files.fs.local;

import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;

import l.files.fs.PathEntry;
import l.files.fs.DirectoryStream;
import l.files.fs.FileSystemException;
import l.files.fs.Path;

final class LocalDirectoryStream implements DirectoryStream {

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
   * @throws FileSystemException if failed to open directory
   */
  static LocalDirectoryStream open(Path directory) throws FileSystemException {
    return open(LocalPath.check(directory).getFile());
  }

  /**
   * @throws FileSystemException if failed to open directory
   */
  static LocalDirectoryStream open(File directory) throws FileSystemException {
    try {
      long dir = Dirent.opendir(directory.getPath());
      return new LocalDirectoryStream(directory, dir);
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
  }

  @Override public void close() throws FileSystemException {
    try {
      Dirent.closedir(dir);
    } catch (ErrnoException e) {
      throw e.toFileSystemException();
    }
  }

  @SuppressWarnings("unchecked")
  Iterable<LocalPathEntry> local() {
    Iterable<?> iterable = this;
    return (Iterable<LocalPathEntry>) iterable;
  }

  @Override
  public Iterator<PathEntry> iterator() {
    if (iterated) {
      throw new IllegalStateException("iterator() has already been called");
    }
    iterated = true;
    return new DirectoryIterator(parent, dir);
  }

  private static final class DirectoryIterator implements Iterator<PathEntry> {
    private final File parent;
    private final long dir;
    private LocalPathEntry next;

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

    @Override public LocalPathEntry next() {
      LocalPathEntry entry = next;
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

    private LocalPathEntry readNext() {
      try {

        Dirent entry;
        do {
          entry = Dirent.readdir(dir);
        } while (entry != null && isSelfOrParent(entry));

        if (entry != null) {
          return LocalPathEntry.create(parent, entry);
        } else {
          return null;
        }

      } catch (ErrnoException e) {
        throw new RuntimeException(e); // TODO
      }
    }

    private boolean isSelfOrParent(Dirent entry) {
      return entry.name().equals(".") || entry.name().equals("..");
    }

    @Override public void remove() {
      throw new UnsupportedOperationException();
    }
  }
}
