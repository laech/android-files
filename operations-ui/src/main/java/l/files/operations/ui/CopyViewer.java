package l.files.operations.ui;

import android.content.Context;

import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

final class CopyViewer extends ProgressViewer {

  CopyViewer(Context context, Clock clock) {
    super(context, clock);
  }

  @Override protected Progress getWork(TaskState.Running state) {
    return state.bytes();
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

  @Override public int getSmallIcon() {
    return R.drawable.ic_stat_notify_copy;
  }
}
