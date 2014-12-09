package l.files.provider;

import java.util.Arrays;

import l.files.fs.FileStatus;

enum SortBy {

  NAME {
    @Override public void sort(FileStatus[] files) {
      sort(files, FileSort.Name.get());
    }
  },

  DATE {
    @Override public void sort(FileStatus[] files) {
      sort(files, FileSort.Date.get());
    }
  },

  SIZE {
    @Override public void sort(FileStatus[] files) {
      sort(files, FileSort.Size.get());
    }
  };

  public abstract void sort(FileStatus[] files);

  void sort(FileStatus[] files, FileSort sort) {
    Arrays.sort(files, sort);
  }

}
