package l.files.operations;

import android.os.Handler;

import de.greenrobot.event.EventBus;
import l.files.fs.Path;
import l.files.fs.Resource;

import static l.files.operations.TaskKind.DELETE;

final class DeleteTask extends Task {

  private final Size count;
  private final Delete delete;

  DeleteTask(int id, Clock clock, EventBus bus, Handler handler,
             Iterable<? extends Resource> resources) {
    super(TaskId.create(id, DELETE), Target.from(resources),
        clock, bus, handler);
    this.count = new Size(resources);
    this.delete = new Delete(resources);
  }

  @Override protected void doTask() throws FileException, InterruptedException {
    count.execute();
    delete.execute();
  }

  @Override protected TaskState.Running running(TaskState.Running state) {
    return state.running(
        Progress.normalize(count.getCount(), delete.getDeletedItemCount()),
        Progress.normalize(count.getSize(), delete.getDeletedByteCount())
    );
  }

}
