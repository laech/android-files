package l.files.sort;

import android.content.res.Resources;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public final class Sorters {

  public static final String NAME = Sort.NAME.id();
  public static final String DATE_MODIFIED = Sort.DATE_MODIFIED.id();

  private Sorters() {}

  /**
   * Gets a list of available sorters in a predefined order suitable for
   * displaying in a list view.
   */
  public static List<Sorter> get() {
    return ImmutableList.<Sorter>of(Sort.NAME, Sort.DATE_MODIFIED);
  }

  /**
   * Applies a sorter with the given sort identifier, if unknown, apply the
   * default sorter.
   */
  public static List<Object> apply(String sort, Resources res, File... files) {
    checkNotNull(sort, "sort");
    try {
      return Sort.valueOf(sort).apply(res, files);
    } catch (IllegalArgumentException e) {
      return Sort.NAME.apply(res, files);
    }
  }
}
