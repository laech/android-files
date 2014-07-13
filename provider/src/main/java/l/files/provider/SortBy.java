package l.files.provider;

import com.google.common.primitives.Longs;

import java.util.Comparator;

enum SortBy implements Comparator<FileData> {

  NAME {
    @Override public int compare(FileData a, FileData b) {
      return a.name.compareToIgnoreCase(b.name);
    }
  },

  DATE {
    @Override public int compare(FileData a, FileData b) {
      int compare = Longs.compare(b.lastModified, a.lastModified);
      if (compare == 0) {
        return NAME.compare(a, b);
      }
      return compare;
    }
  },

  SIZE {
    @Override public int compare(FileData a, FileData b) {
      if (a.directory == 1 && a.directory == b.directory) {
        return NAME.compare(a, b);
      }
      if (a.directory == 1) {
        return 1;
      }
      if (b.directory == 1) {
        return -1;
      }
      int compare = Longs.compare(b.length, a.length);
      if (compare == 0) {
        return NAME.compare(a, b);
      }
      return compare;
    }
  };
}
