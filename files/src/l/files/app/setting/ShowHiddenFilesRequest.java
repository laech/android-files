package l.files.app.setting;

import static com.google.common.base.Objects.toStringHelper;

/**
 * Event representing a request to show/hide hidden files.
 */
public final class ShowHiddenFilesRequest {

  private final boolean show;

  public ShowHiddenFilesRequest(boolean show) {
    this.show = show;
  }

  public boolean show() {
    return show;
  }

  @Override public int hashCode() {
    return show() ? 1 : 0;
  }

  @Override public boolean equals(Object o) {
    return o instanceof ShowHiddenFilesRequest
        && ((ShowHiddenFilesRequest) o).show() == show();
  }

  @Override public String toString() {
    return toStringHelper(this).addValue(show()).toString();
  }
}
