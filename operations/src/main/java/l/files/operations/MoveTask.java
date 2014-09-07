package l.files.operations;

import android.os.Handler;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.util.Collection;
import java.util.List;

import de.greenrobot.event.EventBus;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.transform;

final class MoveTask extends Task implements MoveTaskInfo {

  private final Collection<String> sources;
  private final String dstPath;

  private final Move move;
  private volatile Size count;
  private volatile Copy copy;
  private volatile boolean cleanup;

  MoveTask(int id, EventBus bus, Handler handler,
           Iterable<String> sources, String dstPath) {
    super(id, bus, handler);
    this.sources = ImmutableSet.copyOf(sources);
    this.dstPath = checkNotNull(dstPath, "dstPath");
    this.move = new Move(this.sources, dstPath);
  }

  @Override protected void doTask() throws FileException, InterruptedException {
    try {
      move.execute();
    } catch (FileException e) {
      copyThenDelete(e.failures());
    } finally {
      cleanup = false;
    }
  }

  private void copyThenDelete(List<Failure> failures)
      throws FileException, InterruptedException {
    List<String> paths = transform(failures, new Function<Failure, String>() {
      @Override public String apply(Failure input) {
        return input.path();
      }
    });

    count = new Size(paths);
    count.execute();
    copy = new Copy(paths, dstPath);
    copy.execute();

    cleanup = true;
    new Delete(paths).execute();
  }

  @Override public String getDestinationName() {
    return new File(dstPath).getName();
  }

  @Override public boolean isCleanup() {
    return cleanup;
  }

  @Override public int getTotalItemCount() {
    return count != null ? count.getCount() : sources.size();
  }

  @Override public long getTotalByteCount() {
    return count != null ? count.getSize() : 0;
  }

  @Override public int getProcessedItemCount() {
    return copy != null ? copy.getCopiedItemCount() : move.getMovedItemCount();
  }

  @Override public long getProcessedByteCount() {
    return copy != null ? copy.getCopiedByteCount() : 0;
  }
}
