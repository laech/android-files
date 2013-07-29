package l.files.event;

import com.google.common.base.Objects;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This event will be fired when the viewing preferences are being updated, and
 * it will also be fired on initial registration to the event bus for providing
 * the current viewing preferences.
 */
public final class ViewEvent {

  private final boolean showHiddenFiles;
  private final Sort sort;

  public ViewEvent(Sort sort, boolean showHiddenFiles) {
    this.sort = checkNotNull(sort, "sort");
    this.showHiddenFiles = showHiddenFiles;
  }

  public Sort sort() {
    return sort;
  }

  public boolean showHiddenFiles() {
    return showHiddenFiles;
  }

  @Override public int hashCode() {
    return Objects.hashCode(sort(), showHiddenFiles());
  }

  @Override public boolean equals(Object o) {
    if (o instanceof ViewEvent) {
      ViewEvent that = (ViewEvent) o;
      return Objects.equal(that.sort(), sort())
          && Objects.equal(that.showHiddenFiles(), showHiddenFiles());
    }
    return false;
  }

  @Override public String toString() {
    return toStringHelper(this)
        .add("sort", sort())
        .add("showHiddenFiles", showHiddenFiles())
        .toString();
  }

}
