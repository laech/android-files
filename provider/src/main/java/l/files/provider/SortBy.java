package l.files.provider;

import java.util.Comparator;

import l.files.io.file.FileInfo;

enum SortBy implements Comparator<FileInfo> {

  NAME(FileSort.Name.get()),
  DATE(FileSort.Date.get()),
  SIZE(FileSort.Size.get());

  private final Comparator<FileInfo> delegate;

  private SortBy(Comparator<FileInfo> delegate) {
    this.delegate = delegate;
  }

  @Override public int compare(FileInfo a, FileInfo b) {
    return delegate.compare(a, b);
  }

}
