package l.files.sort;

import android.content.res.Resources;

import java.io.File;
import java.util.List;

enum Sort implements Sorter {

  NAME(new NameSorter()),
  DATE_MODIFIED(new DateModifiedSorter());

  private final SortHelper helper;

  private Sort(SortHelper helper) {
    this.helper = helper;
  }

  @Override public String id() {
    return name();
  }

  @Override public String name(Resources res) {
    return helper.name(res);
  }

  @Override public List<Object> apply(Resources res, File... files) {
    return helper.apply(res, files);
  }
}
