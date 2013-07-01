package l.files.ui.app.files.sort;

import java.util.List;

import android.content.res.Resources;

import com.google.common.collect.ImmutableList;

public final class Sorters {

  /**
   * Gets a list of available sorters in a predefined order suitable for
   * displaying in a list view.
   */
  public static List<Sorter> get(Resources res) {
    return ImmutableList.of(new NameSorter(), new DateModifiedSorter(res));
  }

  private Sorters() {}
}
