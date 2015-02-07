package l.files.operations;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import l.files.fs.Path;

final class FailureRecorder {

  private final List<Failure> failures;
  private final int limit;

  FailureRecorder(int limit) {
    this.limit = limit;
    this.failures = new ArrayList<>();
  }

  public void onFailure(Path path, IOException failure)
      throws FileException {
    if (failures.size() > limit) {
      throw new FileException(failures);
    }
    failures.add(new Failure(path, failure));
  }

  void throwIfNotEmpty() {
    FileException.throwIfNotEmpty(failures);
  }
}
