package l.files.app.sort;

import android.content.res.Resources;
import com.google.common.collect.ImmutableList;
import l.files.app.setting.Sort;

import java.util.List;

public final class Sorters {
  private Sorters() {}

  /**
   * Gets a list of available sorters in a predefined order suitable for
   * displaying in a list view.
   */
  public static List<Sorter> get(Resources res) {
    return ImmutableList.of(new NameSorter(), new DateModifiedSorter(res));
  }

  // TODO
  public static Sorter get(Resources res, Sort sort) {
    switch (sort) {
      case NAME:
        return new NameSorter();
      case DATE_MODIFIED:
        return new DateModifiedSorter(res);
      default:
        throw new AssertionError(sort);
    }
  }
}
