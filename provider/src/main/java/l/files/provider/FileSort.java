package l.files.provider;

import com.google.common.primitives.Longs;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import l.files.io.file.FileInfo;

abstract class FileSort implements Comparator<FileInfo> {

  /**
   * Sort files by name.
   */
  static final class Name extends FileSort {
    private static final Name instance =
        new Name(Locale.getDefault());

    private final Collator collator;

    Name(Locale locale) {
      this.collator = Collator.getInstance(locale);
    }

    public static Name get() {
      return instance;
    }

    @Override public int compare(FileInfo a, FileInfo b) {
      return collator.compare(a.getName(), b.getName());
    }
  }

  /**
   * Sort files by last modified date.
   */
  static final class Date extends FileSort {
    private static final Date instance = new Date();

    Date() {}

    public static Date get() {
      return instance;
    }

    @Override public int compare(FileInfo a, FileInfo b) {
      int compare = Longs.compare(b.getLastModified(), a.getLastModified());
      if (compare == 0) {
        return Name.get().compare(a, b);
      }
      return compare;
    }
  }

  /**
   * Sort files by size.
   */
  static final class Size extends FileSort {
    private static final Size instance = new Size();

    Size() {}

    public static Size get() {
      return instance;
    }

    @Override public int compare(FileInfo a, FileInfo b) {
      if (a.isDirectory() && a.isDirectory() == b.isDirectory()) {
        return Name.get().compare(a, b);
      }
      if (a.isDirectory()) {
        return 1;
      }
      if (b.isDirectory()) {
        return -1;
      }
      int compare = Longs.compare(b.getSize(), a.getSize());
      if (compare == 0) {
        return Name.get().compare(a, b);
      }
      return compare;
    }
  }

}
