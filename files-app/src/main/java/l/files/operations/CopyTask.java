package l.files.operations;

import android.os.Handler;

import de.greenrobot.event.EventBus;
import l.files.fs.Path;
import l.files.fs.Resource;

import static l.files.operations.TaskKind.COPY;

final class CopyTask extends Task {

  private final Size size;
  private final Copy copy;

  CopyTask(int id, Clock clock, EventBus bus, Handler handler,
           Iterable<? extends Resource> sources, Resource destination) {
    super(TaskId.create(id, COPY), Target.from(sources, destination),
        clock, bus, handler);
    this.size = new Size(sources);
    this.copy = new Copy(sources, destination);
  }

  @Override protected void doTask() throws FileException, InterruptedException {
    size.execute();
    copy.execute();
  }

  @Override protected TaskState.Running running(TaskState.Running state) {
    return state.running(
        Progress.normalize(size.getCount(), copy.getCopiedItemCount()),
        Progress.normalize(size.getSize(), copy.getCopiedByteCount())
    );
  }
}
