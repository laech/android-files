package l.files.operations.ui;

import android.content.Context;

import l.files.R;
import l.files.operations.Clock;
import l.files.operations.Progress;
import l.files.operations.TaskState;

import static l.files.common.content.res.Styles.getResourceId;

final class MoveViewer extends ProgressViewer {

  MoveViewer(Context context, Clock system) {
    super(context, system);
  }

  @Override protected Progress getWork(TaskState.Running state) {
    return state.getBytes();
  }

  @Override protected int getTitlePreparing() {
    return R.plurals.preparing_to_move_x_items;
  }

  @Override protected int getTitleRunning() {
    return R.plurals.moving_x_items_to_x;
  }

  @Override protected int getTitleFailed() {
    return R.plurals.fail_to_move;
  }

  @Override public int getSmallIcon(Context context) {
    return getResourceId(android.R.attr.actionModeCutDrawable, context);
  }

}
