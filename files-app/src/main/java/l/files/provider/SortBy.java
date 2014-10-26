package l.files.provider;

import java.util.Arrays;

import l.files.fs.local.LocalFileStatus;

enum SortBy {

  NAME {
    @Override public void sort(LocalFileStatus[] files) {
      sort(files, FileSort.Name.get());
    }
  },

  DATE {
    @Override public void sort(LocalFileStatus[] files) {
      sort(files, FileSort.Date.get());
    }
  },

  SIZE {
    @Override public void sort(LocalFileStatus[] files) {
      sort(files, FileSort.Size.get());
    }
  };

  public abstract void sort(LocalFileStatus[] files);

  void sort(LocalFileStatus[] files, FileSort sort) {
    Arrays.sort(files, sort);
  }

}
