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

public final class DirectoryStream
    implements Iterable<DirectoryStream.Entry>, Closeable {

  private final long dir;
  private boolean iterated;

  private DirectoryStream(long dir) {
    this.dir = dir;
  }

  public static DirectoryStream open(String path) throws IOException {
    long dir = opendir(path);
    return new DirectoryStream(dir);
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
