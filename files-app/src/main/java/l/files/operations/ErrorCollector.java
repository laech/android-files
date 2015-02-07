package l.files.operations;

import java.io.IOException;

import kotlin.Function2;
import kotlin.Unit;
import l.files.fs.Resource;

final class ErrorCollector implements Function2<Resource, IOException, Unit> {

  private final FailureRecorder recorder;

  ErrorCollector(FailureRecorder recorder) {
    this.recorder = recorder;
  }

  @Override public Unit invoke(Resource resource, IOException e) {
    recorder.onFailure(resource.getPath(), e);
    return null;
  }
}
