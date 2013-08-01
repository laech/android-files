package l.files.app.mode;

import android.widget.AbsListView;
import l.files.common.widget.MultiChoiceAction;

import static l.files.trash.TrashService.TrashMover;

public final class Modes {
  private Modes() {}

  public static MultiChoiceAction newCountSelectedItemsAction(AbsListView list) {
    return new CountSelectedItemsAction(list);
  }

  public static MultiChoiceAction newMoveToTrashAction(AbsListView list, TrashMover mover) {
    return new MoveToTrashAction(list, mover);
  }
}
