package l.files.app.category;

import l.files.R;
import l.files.provider.FilesContract;

import static java.lang.System.currentTimeMillis;
import static l.files.provider.FilesContract.Files.SORT_BY_MODIFIED;
import static l.files.provider.FilesContract.Files.SORT_BY_NAME;
import static l.files.provider.FilesContract.Files.SORT_BY_SIZE;

public final class FileCategorizers {
  private FileCategorizers() {}

  /**
   * Gets a categorizer based on the given sort order.
   *
   * @param sortOrder the sort order, may be null
   * @see FilesContract.Files#SORT_BY_NAME
   * @see FilesContract.Files#SORT_BY_MODIFIED
   * @see FilesContract.Files#SORT_BY_SIZE
   */
  public static Categorizer fromSortOrder(String sortOrder) {
    if (SORT_BY_MODIFIED.equals(sortOrder)) {
      return new FileDateCategorizer(currentTimeMillis());
    }
    if (SORT_BY_SIZE.equals(sortOrder)) {
      return new FileSizeCategorizer();
    }
    return Categorizer.NULL;
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
     * @see FilesContract.Files#SORT_BY_NAME
     * @see FilesContract.Files#SORT_BY_MODIFIED
     * @see FilesContract.Files#SORT_BY_SIZE
     */
    public String sortOrder() {
      return sortOrder;
    }

    public int labelId() {
      return labelId;
    }
  }
}
