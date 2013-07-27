package l.files.event;

import l.files.setting.SortBy;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event representing a request for changing the way files are sorted.
 */
public final class SortRequest {

  private final SortBy sort;

  public SortRequest(SortBy sort) {
    this.sort = checkNotNull(sort, "sort");
  }

  public SortBy sort() {
    return sort;
  }

  @Override public int hashCode() {
    return sort().hashCode();
  }

  @Override public boolean equals(Object o) {
    return o instanceof SortRequest
        && ((SortRequest) o).sort().equals(sort());
  }

  @Override public String toString() {
    return toStringHelper(this).addValue(sort()).toString();
  }

}
