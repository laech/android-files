package l.files.operations;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import de.greenrobot.event.EventBus;
import l.files.fs.Resource;

import static l.files.operations.TaskKind.MOVE;

final class MoveTask extends Task {

  private final Move move;
  private final Size count;
  private final Copy copy;

  MoveTask(
      int id,
      Clock clock,
      EventBus bus,
      Handler handler,
      Collection<? extends Resource> sources,
      Resource destination) {

    super(
        TaskId.create(id, MOVE),
        Target.from(sources, destination),
        clock,
        bus,
        handler);

    this.move = new Move(sources, destination);
    this.count = new Size(sources);
    this.copy = new Copy(sources, destination);
  }

  @Override void doTask() throws FileException, InterruptedException {
    try {
      move.execute();
    } catch (FileException e) {
      copyThenDelete(e.failures());
    }
  }

  private void copyThenDelete(List<Failure> failures)
      throws FileException, InterruptedException {

    List<Resource> resources = new ArrayList<>();
    for (Failure failure : failures) {
      resources.add(failure.getResource());
    }
    count.execute();
    copy.execute();
    new Delete(resources).execute();
  }

  @Override TaskState.Running running(TaskState.Running state) {
    return state.running(
        Progress.normalize(count.getCount(), copy.getCopiedItemCount()),
        Progress.normalize(count.getSize(), copy.getCopiedByteCount())
    );
  }
}
