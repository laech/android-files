package l.files.app;

import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Set;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

public final class DeleteFilesRequest {

  private final Set<File> files;

  public DeleteFilesRequest(Iterable<File> files) {
    this.files = ImmutableSet.copyOf(checkNotNull(files, "files"));
  }

  public Set<File> files() {
    return files;
  }

  @Override public int hashCode() {
    return files().hashCode();
  }

  @Override public boolean equals(Object o) {
    return o instanceof DeleteFilesRequest
        && ((DeleteFilesRequest) o).files().equals(files());
  }

  @Override public String toString() {
    return toStringHelper(this).addValue(files()).toString();
  }
}
