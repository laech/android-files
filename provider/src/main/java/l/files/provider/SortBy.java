package l.files.provider;

import com.google.common.primitives.Longs;

import java.util.Comparator;

import l.files.io.file.FileInfo;

enum SortBy implements Comparator<FileInfo> {

  NAME {
    @Override public int compare(FileInfo a, FileInfo b) {
      return a.getName().compareToIgnoreCase(b.getName());
    }
  },

  DATE {
    @Override public int compare(FileInfo a, FileInfo b) {
      int compare = Longs.compare(b.getLastModified(), a.getLastModified());
      if (compare == 0) {
        return NAME.compare(a, b);
      }
      return compare;
    }
  },

  SIZE {
    @Override public int compare(FileInfo a, FileInfo b) {
      if (a.isDirectory() && a.isDirectory() == b.isDirectory()) {
        return NAME.compare(a, b);
      }
      if (a.isDirectory()) {
        return 1;
      }
      if (b.isDirectory()) {
        return -1;
      }
      int compare = Longs.compare(b.getSize(), a.getSize());
      if (compare == 0) {
        return NAME.compare(a, b);
      }
      return compare;
    }
  }

}
