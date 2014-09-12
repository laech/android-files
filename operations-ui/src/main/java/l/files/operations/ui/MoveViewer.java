package l.files.operations.ui;

import android.content.Context;

import l.files.operations.MoveTaskInfo;

final class MoveViewer extends ProgressViewer<MoveTaskInfo> {

  MoveViewer(Context context, Clock system) {
    super(context, system);
  }

  @Override protected long getWorkTotal(MoveTaskInfo value) {
    return value.getTotalByteCount();
  }

  @Override protected long getWorkDone(MoveTaskInfo value) {
    return value.getProcessedByteCount();
  }

  @Override protected String getTargetName(MoveTaskInfo value) {
    return value.getDestinationName();
  }

  @Override protected int getTitlePreparing() {
    return R.plurals.preparing_to_move_x_items;
  }

  @Override protected int getTitleRunning() {
    return R.plurals.moving_x_items_to_x;
  }

  @Override public int getSmallIcon() {
    return R.drawable.ic_stat_notify_cut;
  }
}
