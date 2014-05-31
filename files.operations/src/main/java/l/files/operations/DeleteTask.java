package l.files.operations;

import android.content.Context;
import android.content.Intent;

import java.util.Collections;
import java.util.List;

import l.files.io.file.operations.Count;
import l.files.io.file.operations.Delete;

import static l.files.io.file.operations.FileOperation.Failure;
import static l.files.operations.Progress.STATUS_FINISHED;
import static l.files.operations.Progress.STATUS_PENDING;
import static l.files.operations.Progress.STATUS_PREPRARING;
import static l.files.operations.Progress.STATUS_PROCESSING;

final class DeleteTask extends Task
    implements Count.Listener, Delete.Listener {

  private final Iterable<String> paths;

  private int totalItemCount;
  private int deletedItemCount;

  DeleteTask(Context context, int id, Iterable<String> paths) {
    super(context, id);
    this.paths = paths;
  }

  @Override protected Intent getPendingMessage() {
    return newProgress(STATUS_PENDING);
  }

  @Override protected Intent getFinishedMessage(List<Failure> result) {
    return newProgress(STATUS_FINISHED, result);
  }

  @Override protected void doTask() throws InterruptedException {
    new Count(this, paths).call();
    new Delete(this, paths).call();
  }

  @Override public void onCount(String path) {
    totalItemCount++;
    if (setAndGetUpdateProgress()) {
      publishProgress(newProgress(STATUS_PREPRARING));
    }
  }

  @Override public void onDelete(String path) {
    deletedItemCount++;
    if (setAndGetUpdateProgress()) {
      publishProgress(newProgress(STATUS_PROCESSING));
    }
  }

  private Intent newProgress(int status) {
    return newProgress(status, Collections.<Failure>emptyList());
  }

  private Intent newProgress(int status, List<Failure> failures) {
    return Progress.Delete.create(id(), startTime(), status, failures,
        totalItemCount, deletedItemCount);
  }
}
