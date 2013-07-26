package l.files.event;

import java.io.File;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

abstract class FileEvent {

  private final File file;

  FileEvent(File file) {
    this.file = checkNotNull(file, "file");
  }

  public final File file() {
    return file;
  }

  @Override public final int hashCode() {
    return file().hashCode();
  }

  @Override public final boolean equals(Object o) {
    return o instanceof FileEvent && ((FileEvent) o).file().equals(file());
  }

  @Override public final String toString() {
    return toStringHelper(this).addValue(file()).toString();
  }

}
