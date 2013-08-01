package l.files.app.setting;

import java.io.File;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Event representing a request for a bookmark to be added.
 */
public final class AddBookmarkRequest {

  private final File file;

  public AddBookmarkRequest(File file) {
    this.file = checkNotNull(file, "file");
  }

  public File file() {
    return file;
  }

  @Override public final int hashCode() {
    return file().hashCode();
  }

  @Override public final boolean equals(Object o) {
    return o instanceof AddBookmarkRequest
        && ((AddBookmarkRequest) o).file().equals(file());
  }

  @Override public final String toString() {
    return toStringHelper(this).addValue(file()).toString();
  }
}
