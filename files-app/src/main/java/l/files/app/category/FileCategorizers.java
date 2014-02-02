package l.files.app.category;

import l.files.R;

import static com.google.common.base.Strings.nullToEmpty;
import static java.lang.System.currentTimeMillis;
import static l.files.provider.FilesContract.FileInfo;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_MODIFIED;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_NAME;
import static l.files.provider.FilesContract.FileInfo.SORT_BY_SIZE;

public final class FileCategorizers {
  private FileCategorizers() {}

  /**
   * Gets a categorizer based on the given sort order.
   *
   * @param sortOrder the sort order, may be null
   * @see FileInfo#SORT_BY_NAME
   * @see FileInfo#SORT_BY_MODIFIED
   * @see FileInfo#SORT_BY_SIZE
   */
  public static Categorizer fromSortOrder(String sortOrder) {
    switch (nullToEmpty(sortOrder)) {
      case SORT_BY_MODIFIED:
        return new FileDateCategorizer(currentTimeMillis());
      case SORT_BY_SIZE:
        return new FileSizeCategorizer();
      case SORT_BY_NAME:
      default:
        return Categorizer.NULL;
    }
  }

  /**
   * Gets the available sort options.
   */
  public static SortOption[] getSortOptions() {
    return SortOption.values();
  }

  public static enum SortOption {
    NAME(SORT_BY_NAME, R.string.name),
    MODIFIED(SORT_BY_MODIFIED, R.string.date_modified),
    SIZE(SORT_BY_SIZE, R.string.size);

    private final String sortOrder;
    private final int labelId;

    SortOption(String sortOrder, int labelId) {
      this.sortOrder = sortOrder;
      this.labelId = labelId;
    }

    /**
     * @see FileInfo#SORT_BY_NAME
     * @see FileInfo#SORT_BY_MODIFIED
     * @see FileInfo#SORT_BY_SIZE
     */
    public String sortOrder() {
      return sortOrder;
    }

    public int labelId() {
      return labelId;
    }
  }
}