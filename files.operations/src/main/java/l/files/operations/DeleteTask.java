package l.files.operations;

import java.util.Collections;
import java.util.List;

import l.files.io.file.operations.Count;
import l.files.io.file.operations.Delete;

import static l.files.io.file.operations.FileOperation.Failure;
import static l.files.operations.Progress.State;
import static l.files.operations.Progress.State.FINISHED;
import static l.files.operations.Progress.State.PENDING;
import static l.files.operations.Progress.State.PREPARING;
import static l.files.operations.Progress.State.PROCESSING;

final class DeleteTask extends Task
    implements Count.Listener, Delete.Listener {

  private final Iterable<String> paths;

  private int totalItemCount;
  private int deletedItemCount;

  DeleteTask(int id, Iterable<String> paths) {
    super(id);
    this.paths = paths;
  }

  @Override protected Object getPendingMessage() {
    return newProgress(PENDING);
  }

  @Override protected Object getFinishedMessage(List<Failure> result) {
    return newProgress(FINISHED, result);
  }

  @Override protected void doTask() throws InterruptedException {
    new Count(this, paths).call();
    new Delete(this, paths).call();
  }

  @Override public void onCount(String path) {
    totalItemCount++;
    if (setAndGetUpdateProgress()) {
      publishProgress(newProgress(PREPARING));
    }
  }

  @Override public void onDelete(String path) {
    deletedItemCount++;
    if (setAndGetUpdateProgress()) {
      publishProgress(newProgress(PROCESSING));
    }
  }

  private DeleteProgress newProgress(State state) {
    return newProgress(state, Collections.<Failure>emptyList());
  }

  private DeleteProgress newProgress(State state, List<Failure> failures) {
    return DeleteProgress.create(id(), startTime(), state, failures,
        totalItemCount, deletedItemCount);
  }
}
