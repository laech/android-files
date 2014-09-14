package l.files.operations.ui;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

public final class CopyViewerTest extends ProgressViewerTest {

  @Override protected CopyViewer create(Context context, Clock clock) {
    return new CopyViewer(context, clock);
  }

  @Override protected TaskState.Running setProgress(
      TaskState.Running state, Progress progress) {
    return state.running(state.items(), progress);
  }

  @Override protected int getTitlePreparing() {
    return R.plurals.preparing_to_copy_x_items_to_x;
  }

  @Override protected int getTitleRunning() {
    return R.plurals.copying_x_items_to_x;
  }

  @Override protected int getTitleFailed() {
    return R.plurals.fail_to_copy;
  }

  @Override protected int getSmallIcon() {
    return R.drawable.ic_stat_notify_copy;
  }
}
