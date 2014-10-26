package l.files.provider;

import java.util.Arrays;

import l.files.fs.local.FileInfo;

enum SortBy {

  NAME {
    @Override public void sort(FileInfo[] files) {
      sort(files, FileSort.Name.get());
    }
  },

  DATE {
    @Override public void sort(FileInfo[] files) {
      sort(files, FileSort.Date.get());
    }
  },

  SIZE {
    @Override public void sort(FileInfo[] files) {
      sort(files, FileSort.Size.get());
    }
  };

  public abstract void sort(FileInfo[] files);

  void sort(FileInfo[] files, FileSort sort) {
    Arrays.sort(files, sort);
  }

}
